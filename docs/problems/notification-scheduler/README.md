# 02. 알림 스케줄러

> **축** 신뢰성 · 처리량 | **우선순위** 2 | **상태** 진행 중
> 블루넥스 인턴에서 다룬 대량 발송 문제를 **다른 규모·다른 제약**에서 다시 만나는 사례.

원래 한 문서에 여섯 갈래로 적어 두었으나, **결과가 다른 두 축**이라 글을 나눈다.

---

## 두 글로 나눈 기준

**결과가 다르면 나누고, 같으면 합친다.**

| | 다루는 것 | 결과 |
|---|---|---|
| [`retry/`](retry/) | 못 보낸 알림을 다시 처리할 수 있는가 | 유실 N건 → 0 |
| [`throughput/`](throughput/) | 밀린 알림을 얼마나 빨리 처리하는가 | 드레인 시간 · 수용 건수 |

원래 여섯 갈래는 이렇게 갈린다.

```
retry/        2-1  발송 실패해도 완료 처리       ┐ 둘 다 "유실"
              2-2  조회 범위가 미래 1분뿐        ┘
              2-3  발송 후 개별 저장 (중복)       재처리를 넣으면 드러나는 부작용
              2-4  스케줄러 락 없음              단일 인스턴스라 지금은 없는 문제. 남은 것으로 기록

throughput/   2-5  동기 순차 발송               ┐ 둘 다 "지연"
              2-6  N+1과 트랜잭션 부재          ┘
```

---

## 순서

**재처리를 먼저 한다.**

```
① 재처리   지금 실제로 유실이 나고 있다
② 처리량   재처리를 넣어야 "밀린 게 쌓인다"는 동기가 생긴다
```

처리량을 먼저 하면 **지금 나지 않는 문제를 먼저 고친 것**이 된다.
그리고 재처리 구조는 겹쳐서 밀린 경우의 피해까지 흡수하므로, 처리량 문제는
재처리 이후 **"유실"에서 "지연"으로 격이 내려간다.**

---

## 현재 코드

`src/main/java/com/toit/notification/alarm/NotificationScheduler.java`

```java
@Scheduled(cron = "0 * * * * *")                                // 매 분 0초에 실행
public void checkAndSendAlerts() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    LocalDateTime nextMinute = now.plusMinutes(1);

    List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, nextMinute);
    if (alarms.isEmpty()) return;

    for (SchedulesAlarm alarm : alarms) {
        Schedules schedule = alarm.getSchedules();              // (a) N+1
        Users user = schedule.getUsers();                       // (a) N+1

        UserNotification notification = userNotificationService.create(...);

        boolean isSent = fcmNotificationService.sendToUser(user, ...);   // (b) 동기 호출

        if (isSent) {
            userNotificationService.markAsSent(notification);
        }

        alarm.markAsSent();                                     // (c) 무조건 실행
        schedulesAlarmRepository.save(alarm);
    }
}
```

조회 쿼리 — `SchedulesAlarmRepository.findTargetAlarms`

```sql
WHERE a.alarmDateTime >= :start AND a.alarmDateTime < :end
  AND a.alarmState = true
  AND a.isSent = false
  AND s.status = 'ACTIVE'
  AND us.appAlarmEnabled = true
```

**메서드에 `@Transactional`이 없다.**

---

## 관련 작업 이력

| 시점 | 내용 |
|---|---|
| 2026-08-23 | FCM 재활성화 (PR #253). 주석으로 꺼져 있던 발송·토큰·알림 예약을 되살림 |
| 2026-08-23 | `[ALARM]` 태그 추적 로그 추가. 설정 → 예약 → 조회 → 발송 → 완료 전 구간 |
| 2026-08-23 | 일정 날짜가 하루 앞당겨 저장되던 문제 수정 (컨테이너 시간대) |
| 2026-08-24 | 패키지 구조 정리 (PR #254). 알림 관련이 네 곳에 흩어져 있던 것을 `notification/{alarm,push,inbox}` 로 통합 |
| 2026-08-24 | 실기기로 설정부터 수신까지 전 구간 동작 확인 |
