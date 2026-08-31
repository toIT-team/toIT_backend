package com.toit.notification.alarm;

import com.toit.notification.push.FcmNotificationService;
import com.toit.notification.push.FcmSendResult;
import com.toit.notification.push.request.FcmNotificationRequest;
import com.toit.notification.inbox.NotificationType;
import com.toit.notification.inbox.UserNotification;
import com.toit.notification.inbox.UserNotificationService;
import com.toit.schedules.Schedules;
import com.toit.user.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    /** 최초 발송 뒤 몇 번까지 다시 보낼지. 3번을 쓰면 1+2+4 로 7분을 덮는다. */
    private static final int MAX_ATTEMPT = 3;

    /**
     * 얼마나 지난 알림까지 되살릴지.
     *
     * 사용자가 약속한 것은 일정과의 거리가 아니라 **알림 시각** 자체다. 4시로
     * 맞춘 알림은 오프셋이 5분이든 하루든 4시에 와야 한다. 그래서 유예도 오프셋과
     * 무관하게 하나로 둔다.
     *
     * 10분인 이유는 두 가지다.
     *   - 그쯤까지는 "좀 늦었네" 지만 그 뒤로는 "고장났네" 로 읽힌다고 봤다.
     *     공식 기준은 없어 우리가 정한 값이다.
     *   - 재시도 3회가 1+2+4 로 7분을 쓰므로, 마지막 재시도까지 이 안에 들어간다.
     *     창을 7분 밑으로 내리면 넣어둔 재시도가 잘린다.
     *
     * FCM 쪽에도 같은 개념이 있다. 공식 문서가 짧은 TTL 이 필요한 예로 캘린더
     * 알림을 든다. 다만 몇 분으로 하라는 기준은 없어 그 값은 우리가 정했다.
     * https://firebase.google.com/docs/cloud-messaging/customize-messages/setting-message-lifespan
     */
    private static final long VALID_MINUTES = 10;

    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final FcmNotificationService fcmNotificationService;
    private final UserNotificationService userNotificationService;
    private final Clock clock;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendAlerts() {
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime oldest = now.minusMinutes(VALID_MINUTES);

        // "지금 이 1분에 울릴 것" 이 아니라 "아직 안 보낸 것" 을 가져온다.
        // 서버가 꺼졌던 사이에 지나간 알림도 여기서 회수된다.
        List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, oldest);

        log.info("[ALARM] 조회 now={} oldest={} count={}", now, oldest, alarms.size());
        if (alarms.isEmpty()) return;

        for (SchedulesAlarm alarm : alarms) {
            Schedules schedule = alarm.getSchedules();
            Users user = schedule.getUsers();

            String title = schedule.getTitle();
            String body = bodyOf(schedule, now);

            log.info("[ALARM] 발송시도 alarmId={} usersId={} scheduleId={} 예정={} 재시도={}",
                    alarm.getSchedulesAlarmId(), user.getUsersId(),
                    schedule.getSchedulesId(), alarm.getAlarmDateTime(), alarm.getAttemptCount());

            // 재시도해도 알림함에는 한 줄만 남는다. 일정을 미뤄 울릴 시각이 바뀌면
            // 키도 바뀌는데, 그건 다른 알림이니 새 줄이 맞다.
            UserNotification notification = userNotificationService.findOrCreate(
                    idempotencyKeyOf(alarm),
                    user,
                    NotificationType.SCHEDULE,
                    title,
                    "toit://schedule?id=" + schedule.getSchedulesId(),
                    schedule.getSchedulesId()
            );

            FcmSendResult result = fcmNotificationService.send(
                    user,
                    new FcmNotificationRequest(
                            title,
                            body,
                            "schedule_detail",
                            notification.getDeeplink(),
                            notification.getNotificationId()
                    )
            );

            applyResult(alarm, notification, result, now);
            schedulesAlarmRepository.save(alarm);
        }
    }

    /**
     * 알림 문구를 만든다.
     *
     * 저장해 둔 오프셋을 그대로 쓰면 발송이 밀렸을 때 문구가 거짓이 된다.
     * 5분 늦게 나간 알림이 여전히 "5분 전입니다" 라고 말한다. 그래서 오프셋이
     * 아니라 **보내는 시점에 남은 시간**으로 적는다.
     *
     * 단위는 남은 시간에 맞춘다. 하루 전 알림에 "1440분 전입니다" 는 읽히지 않는다.
     */
    private String bodyOf(Schedules schedule, LocalDateTime now) {
        // 종일 일정은 울릴 시각이 시작 날짜 오전 9시로 고정이라 남은 시간이 아니라
        // 날짜 차이로 말한다.
        if (!Boolean.TRUE.equals(schedule.getTimeSetting()) || schedule.getStartTime() == null) {
            long days = ChronoUnit.DAYS.between(now.toLocalDate(), schedule.getStartDate());
            return days <= 0 ? "오늘 일정입니다." : days + "일 뒤 일정입니다.";
        }

        LocalDateTime start = LocalDateTime.of(schedule.getStartDate(), schedule.getStartTime());
        Duration left = Duration.between(now, start);

        if (left.isNegative() || left.isZero()) {
            return "일정이 곧 시작됩니다.";
        }
        if (left.toDays() >= 1) {
            return "일정이 시작되기 " + left.toDays() + "일 전입니다.";
        }
        if (left.toHours() >= 1) {
            return "일정이 시작되기 " + left.toHours() + "시간 전입니다.";
        }
        // 59초가 남아도 "0분 전" 이 아니라 "1분 전" 이 자연스럽다.
        return "일정이 시작되기 " + Math.max(1, left.toMinutes()) + "분 전입니다.";
    }

    /**
     * 발송 결과에 따라 상태를 정한다.
     *
     * 건마다 따로 저장한다. 100건을 돌다 중간에 죽어도 앞의 것은 SENT 로 남는다.
     * 하나의 트랜잭션으로 묶었다면 전부 롤백되어 다시 나갔을 것이다.
     */
    private void applyResult(SchedulesAlarm alarm, UserNotification notification,
                             FcmSendResult result, LocalDateTime now) {
        switch (result.outcome()) {
            case SENT -> {
                userNotificationService.markAsSent(notification);
                alarm.markAsSent();
                log.info("[ALARM] 발송성공 alarmId={} notificationId={}",
                        alarm.getSchedulesAlarmId(), notification.getNotificationId());
            }
            // 보낼 토큰이 없다. 몇 분 뒤에도 없으므로 재시도할 이유가 없다.
            // 사용자가 앱을 다시 열면 새 토큰이 등록되어 다음 알림부터 받는다.
            case NO_TOKEN, ALARM_OFF -> {
                alarm.markAsFailed(result.errorCode());
                log.warn("[ALARM] 발송포기 alarmId={} 사유={}",
                        alarm.getSchedulesAlarmId(), result.errorCode());
            }
            // 보낼 데는 있는데 실패했다. 응답을 못 받은 경우도 여기로 온다.
            case FAILED -> {
                if (alarm.getAttemptCount() >= MAX_ATTEMPT) {
                    alarm.markAsFailed(result.errorCode());
                    log.warn("[ALARM] 재시도소진 alarmId={} 시도={} 사유={}",
                            alarm.getSchedulesAlarmId(), alarm.getAttemptCount(), result.errorCode());
                } else {
                    LocalDateTime next = now.plusMinutes(backoffMinutes(alarm.getAttemptCount()));
                    alarm.scheduleNextAttempt(next, result.errorCode());
                    log.warn("[ALARM] 재시도예약 alarmId={} 시도={} 다음={} 사유={}",
                            alarm.getSchedulesAlarmId(), alarm.getAttemptCount(), next, result.errorCode());
                }
            }
        }
    }

    /**
     * 재시도 간격을 두 배씩 벌린다. 1 · 2 · 4분.
     *
     * 매 분 시도하면 3번을 3분 만에 다 써서, FCM 이 5분 멈추면 그대로 버린다.
     * 같은 3번으로 7분을 덮으면 그만큼 긴 장애를 견딘다.
     */
    private long backoffMinutes(int attemptCountBefore) {
        return 1L << attemptCountBefore;
    }

    /** alarm:{예약번호}:{울릴시각} — 시각이 바뀌면 다른 알림이므로 키도 바뀐다. */
    private String idempotencyKeyOf(SchedulesAlarm alarm) {
        return "alarm:" + alarm.getSchedulesAlarmId() + ":" + alarm.getAlarmDateTime();
    }
}
