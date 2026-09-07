# 발송 중에 일정이 수정되면 그 수정이 날아간다

> 본문: [알림 처리량 개선기](README.md)

동시 발송을 넣으면서 발견한 것. **아직 안 고쳤다.**

---

## 무엇이 문제인가

발송 스레드는 알림을 **읽고 → FCM 에 보내고 → 결과를 저장**한다. 읽기와 저장 사이에
FCM 왕복 330ms 가 비어 있다.

그 사이에 사용자가 일정을 수정하면, 스레드가 들고 있던 옛 객체를 저장하면서
**수정이 덮어써진다.**

```
14:30:00.0   스레드가 읽는다        alarm_date_time = 14:30, attempt = 1
14:30:00.1   사용자가 15:00 으로 미룬다
             → DB 는 15:00, status = PENDING, attempt = 0
14:30:00.3   FCM 응답이 온다
14:30:00.3   save(alarm)            ← 들고 있던 옛 객체
             → DB 가 다시 14:30, attempt = 2 로 되돌아간다
```

**사용자는 15:00 으로 옮겼는데 알림은 계속 14:30 을 가리킨다.**

`SchedulesAlarm.save()` 가 필드를 통째로 덮어쓰기 때문이다. 바뀐 것만 골라 쓰지 않는다.

---

## 왜 DB 가 안 막아주나

`SchedulesAlarm` 에 `@Version` 이 없다. 낙관적 락이 없으니 **나중에 저장한 쪽이 그냥
이긴다.**

`UserNotification` 에도 없다. 다만 그쪽은 멱등키로 한 줄만 만들어 쓰므로 이 문제가
덜하다.

---

## 지금은 왜 안 터졌나

세 가지가 겹쳐서다.

```
한 번에 진행 중인 알림이 1건        스레드가 하나라서
사용자가 32명                      수정이 발송과 겹칠 일이 드묾
창이 330ms                        짧다
```

**동시성을 8로 올리면 첫 줄이 8건이 된다.** 부딪힐 창이 여덟 배가 된다. 빈도가
아니라 **한 번에 노출된 건수**가 늘어나는 것이다.

---

## 옛 재시도가 남지는 않는다

처음에 걱정한 것과 달리, 일정을 수정해도 **재시도가 두 갈래로 갈리지는 않는다.**

`schedules_alarm` 은 일정 하나당 한 행이고, `updateAlarm` 이 그 행을 덮어쓴다.

```java
public void updateAlarm(...) {
    this.alarmDateTime = alarmDateTime;   // 14:30 → 15:00
    this.status = AlarmStatus.PENDING;
    this.attemptCount = 0;                // 재시도 이력이 초기화된다
    this.nextAttemptAt = null;
}
```

옛 시각의 재시도 예약은 사라진다. 알림함 줄만 둘이 남는데(`alarm:3021:14:30` 과
`alarm:3021:15:00`), 그건 다른 시각에 울린 다른 알림이므로 맞는 동작이다.

---

## 고치는 방법 둘

### A. `@Version` 을 붙인다

```java
@Version
private Long version;
```

수정이 끼어들면 저장할 때 `ObjectOptimisticLockingFailureException` 이 난다.
`AlarmSendService.sendOne` 의 `try/catch` 가 잡고, **그 건은 다음 실행이 다시
처리**한다.

```
장점   한 줄. 이 경우 말고 다른 경합도 같이 잡는다
단점   컬럼 추가 마이그레이션이 필요하다
       충돌한 건은 그 실행에서 버려진다 (다음 실행이 회수하므로 유실은 아니다)
```

### B. 발송 결과만 부분 업데이트한다

```java
@Modifying
@Query("update SchedulesAlarm a set a.status = :status, a.attemptCount = :count, " +
       "a.nextAttemptAt = :next, a.lastErrorCode = :code " +
       "where a.schedulesAlarmId = :id")
```

`alarm_date_time` 을 아예 안 건드리니 **충돌 자체가 생기지 않는다.**

```
장점   충돌을 없앤다. 마이그레이션이 없다
단점   쿼리를 따로 쓰고, 필드가 늘 때마다 같이 고쳐야 한다
       엔티티 메서드(markAsSent 등)와 저장 경로가 갈린다
```

---

## 아직 안 정한 것

**어느 쪽으로 갈지.** A 가 단순하고 표준적이라 기울지만, B 는 충돌 자체를 없앤다.

**언제 할지.** 동시성을 넣기 전에 하는 편이 낫다. 넣고 나면 창이 여덟 배가 되므로
그 상태로 배포하면 노출이 커진다.

**재현 테스트를 만들지.** 330ms 창을 노려 수정을 끼워 넣어야 해서 손으로는 어렵다.
`@Version` 을 붙이면 충돌이 예외로 드러나므로, 그 예외가 나는지로 대신 확인할 수 있다.
