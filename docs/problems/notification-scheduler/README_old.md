# 02. 알림 스케줄러의 유실과 중복 발송

> **축** 동시성 · 신뢰성 | **우선순위** 2 | **상태** 미착수 (현재 FCM 발송 자체가 비활성화)
> 블루넥스 인턴에서 다룬 대량 발송 문제를 **다른 규모·다른 제약**에서 다시 만나는 사례.

---

## 1. 현재 코드

`src/main/java/com/toit/fcm/notification/NotificationScheduler.java`

전체가 주석 처리되어 있습니다(`// [FCM 비활성화] 정기 알림 발송 스케줄러 중단`).
아래는 주석을 걷어낸 이전 구현입니다.

```java
@Scheduled(cron = "0 * * * * *")                                    // 매 분 실행
public void checkAndSendAlerts() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    LocalDateTime nextMinute = now.plusMinutes(1);

    List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, nextMinute);
    if (alarms.isEmpty()) return;

    for (SchedulesAlarm alarm : alarms) {
        Schedules schedule = alarm.getSchedules();                  // (a) N+1
        Users user = schedule.getUsers();                           // (a) N+1

        UserNotification notification = userNotificationService.create(...);

        boolean isSent = fcmNotificationService.sendToUser(user, ...);   // (b) 동기 호출

        if (isSent) {
            userNotificationService.markAsSent(notification);
        }

        alarm.markAsSent();                                         // (c) 무조건 실행
        schedulesAlarmRepository.save(alarm);
    }
}
```

관련 파일

| 파일 | 내용 |
|---|---|
| `NotificationScheduler.java:30` | `@Scheduled(cron = "0 * * * * *")` |
| `SchedulesAlarmRepository.java:32` | `findTargetAlarms(start, end)` |
| `FcmNotificationService.java` | `sendToUser` — 현재 `return false`로 단락 |

**메서드에 `@Transactional`이 없습니다.**

---

## 2. 무엇이 문제인가

### 2-1. 발송 실패해도 완료 처리 — 영구 유실 (가장 명확한 버그)

```java
if (isSent) {
    userNotificationService.markAsSent(notification);   // 조건부
}
alarm.markAsSent();                                     // ← 조건 없음
```

`isSent`가 `false`여도 `alarm.markAsSent()`는 실행됩니다.
FCM 호출이 실패한 알람이 **"발송 완료"로 마킹되어 다시는 조회되지 않습니다.**

`UserNotification`은 미발송 상태로 남지만 재발송을 시도하는 경로가 없어 실질적으로 유실입니다.

### 2-2. 조회 범위가 미래 1분뿐 — 놓치면 영구 소실

```java
LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, now.plusMinutes(1));
```

범위가 `[now, now+1분)`이라 **과거 미발송분을 절대 잡지 않습니다.**

- 배포·재시작으로 그 분의 실행을 건너뛰면 → 해당 분의 알림 전부 소실
- 이전 실행이 1분을 넘겨 다음 실행이 밀리면 → 밀린 구간 소실
- 장애로 스케줄러가 몇 분간 멈추면 → 그 구간 전부 소실

`isSent = false`인 과거 알람이 DB에 남아 있어도 **다시 조회되지 않습니다.**

### 2-3. 발송 후 개별 저장 — 중복 발송

FCM 호출 성공 → `markAsSent()` → `save()` 사이에 프로세스가 죽으면,
다음 실행에서 같은 알람을 다시 조회해 **재발송**합니다.

**멱등키가 없어 수신자 입장에서 같은 알림을 두 번 받습니다.**
블루넥스에서 겪은 "재실행 시 중복 수신"과 정확히 같은 구조입니다.

### 2-4. 스케줄러 락 없음 — 인스턴스 증설 시 전량 중복

`@Scheduled`는 각 인스턴스에서 독립적으로 실행됩니다.
서버를 2대로 늘리는 순간 **모든 알림이 2번씩 발송**됩니다.

현재는 단일 인스턴스라 드러나지 않지만, **구조적으로 수평 확장이 불가능한 상태**입니다.

### 2-5. 동기 순차 발송 — 스케줄 겹침

```java
for (SchedulesAlarm alarm : alarms) {
    fcmNotificationService.sendToUser(user, ...);   // 네트워크 호출, 동기
}
```

FCM 호출이 건당 100ms라고 가정하면:

| 알람 수 | 소요 시간 | 결과 |
|---|---|---|
| 100건 | 10초 | 정상 |
| 600건 | 60초 | **다음 스케줄과 경계** |
| 1,000건 | 100초 | **다음 실행과 겹침 → 2-4와 같은 중복 위험** |

`@Scheduled`는 기본적으로 단일 스레드로 동작하므로 겹치면 뒤 실행이 밀리고,
밀린 만큼 2-2의 조회 범위를 벗어나 유실로 이어집니다.

### 2-6. N+1과 트랜잭션 부재

- `alarm.getSchedules()` → `schedule.getUsers()`로 알람마다 추가 쿼리
  (`findTargetAlarms`에 `JOIN FETCH`가 없음 — 같은 리포지토리의 `findSentAlarmsByUsersId:36`에는 있음)
- `@Transactional`이 없어 지연 로딩 시점의 영속성 컨텍스트 보장이 불확실
- `markAsSent()` 후 `save()`를 명시 호출하는 것도 트랜잭션 부재의 흔적

---

## 3. 재현 계획

### 3-1. 확인할 것

| 시나리오 | 방법 | 확인할 것 |
|---|---|---|
| 유실 (2-1) | FCM 호출을 강제 실패시킴 | `isSent=false`인데 `alarm.isSent=true`로 마킹되는가 |
| 유실 (2-2) | 스케줄러 실행을 1분 건너뜀 | 해당 분 알람이 이후 영원히 조회되지 않는가 |
| 중복 (2-3) | `markAsSent` 직전에 프로세스 종료 | 다음 실행에서 재발송되는가 |
| 중복 (2-4) | 인스턴스 2개 동시 기동 | 알림이 정확히 2배로 발송되는가 |
| 겹침 (2-5) | 알람 1,000건 + FCM 지연 주입 | 실행이 1분을 넘겨 다음 스케줄과 겹치는가 |

### 3-2. 기록할 지표

- **유실률** — 발송 대상 대비 실제 도달하지 않은 비율
- **중복률** — 수신자 기준 중복 수신 건수 / 전체
- **발송 지연 p95** — 예정 시각 대비 실제 발송 시각
- **스케줄 겹침 발생률** — 실행 시간이 주기를 초과한 비율
- **인스턴스 2개일 때 중복 발생률** — 이론상 100%인지 실측

> FCM은 실제 발송 대신 **호출을 가로채는 스텁**으로 대체해 측정합니다.
> 실제 기기로 보내면 재현이 어렵고 비용도 발생합니다.

---

## 4. 고려할 대안

| 대안 | 방식 | 트레이드오프 |
|---|---|---|
| **A. 스케줄러 락 (ShedLock)** | DB/Redis 락으로 한 인스턴스만 실행 | 중복(2-4)은 막지만 **단일 실행이라 처리량 한계**. 2-1·2-2·2-3은 그대로 |
| **B. 조회 범위 확장 + 상태 기반** | `WHERE isSent = false AND alarmDateTime <= now`로 변경 | 유실(2-2) 해결. 다만 장애 복구 시 **밀린 알림이 한꺼번에 폭주**. 오래된 알림을 보낼지 버릴지 정책 필요 |
| **C. 상태 전이 + 멱등키** | `PENDING → SENDING → SENT/FAILED` + UNIQUE 제약 | 중복(2-3) 해결. `SENDING`에서 멈춘 것의 타임아웃 회수 로직 필요 |
| **D. Outbox + 별도 워커** | 발송 작업을 테이블에 적재하고 워커가 소비 | 정합성 좋음. **컴포넌트 증가**, 워커 자체의 중복 실행 문제는 남음 |
| **E. RabbitMQ 위임 (인턴 때 방식)** | 큐가 선점·분배·재시도·DLQ를 담당 | 검증된 방식. 다만 **2 vCPU / 2GB LightSail에 큐를 올리는 비용**. 현재 발송량 대비 과한 인프라일 수 있음 |
| **F. 발송 자체를 비동기화** | 조회는 동기, FCM 호출은 스레드 풀 | 겹침(2-5) 완화. 스레드 풀 크기·거부 정책 설계 필요 |

### 반드시 쓸 서술

**인턴에서는 RabbitMQ가 정답이었지만, 여기서는 다른 선택이 맞을 수 있습니다.**

| | 블루넥스 | toIT |
|---|---|---|
| 발송량 | 10만 건 일괄 | 분당 수십 건 |
| 인프라 | 별도 서버 | 2 vCPU / 2GB 단일 인스턴스 |
| 발송 패턴 | 관리자 트리거 대량 | 일정 시각 분산 |

**같은 문제라도 규모와 제약이 다르면 답이 달라진다**는 것을 근거와 함께 보여주는 것이
이 문제를 다루는 가장 큰 가치입니다. 무조건 인턴 때 방식을 가져오면 오히려 판단력이 없어 보입니다.

---

## 5. 파고들 지점

### 5-1. 전달 보장 수준

- **at-most-once** — 중복 없음, 유실 가능 (현재 구조가 지향한 듯하나 실제로는 둘 다 발생)
- **at-least-once** — 유실 없음, 중복 가능 → **멱등성으로 중복을 흡수**
- **exactly-once** — 분산 환경에서는 사실상 불가능. at-least-once + 멱등 처리가 현실적 해법

→ toIT 알림에 맞는 수준은 무엇인지, 그 근거는 무엇인지 정리하세요.

### 5-2. 연결되는 개념

- **멱등성** — 멱등키 설계, UNIQUE 제약, 중복 요청 식별 단위
- **분산 락** — 락 획득 후 프로세스가 죽으면? TTL, 펜싱 토큰
- **재시도 정책** — 오류 유형 구분이 핵심
  - `UNREGISTERED` / `INVALID_ARGUMENT` → **재시도 금지, 토큰 삭제** (현 코드 `FcmNotificationService`에 이미 구현되어 있음)
  - `UNAVAILABLE` / `INTERNAL` → 지수 백오프 + jitter로 재시도
  - 무한 재시도 방지를 위한 최대 횟수와 DLQ
- **스레드** — `@Scheduled`의 기본 단일 스레드, `ThreadPoolTaskScheduler`, 스케줄 겹침
- **트랜잭션 경계** — 외부 API 호출을 트랜잭션 안에 두면 안 되는 이유(커넥션 점유)

### 5-3. Ecole 42와의 연결

여러 인스턴스가 같은 알람을 집는 문제는 **상호배제**이고,
락을 잡은 인스턴스가 죽었을 때의 처리는 **점유대기와 비선점**의 문제입니다.
C에서 뮤텍스로 다룬 것을 분산 환경으로 확장한 형태입니다.

---

## 6. 완료 기준

- [ ] 5가지 문제(2-1 ~ 2-5)를 각각 **재현 테스트로 증명**하고 개선 전 숫자 확보
- [ ] 인스턴스 2개 기동 시 중복률 실측 (이론상 100%인지)
- [ ] 대안 중 최소 3개 비교, 특히 **RabbitMQ를 쓰지 않기로 한 근거**를 수치로 뒷받침
- [ ] 개선 후 유실률 0%, 중복률 0% 확인
- [ ] 알람 1,000건 기준 스케줄 겹침 미발생 확인
- [ ] 인턴 경험과의 비교표(규모·제약·선택) 정리
- [ ] `blog/` 에 글로 정리
