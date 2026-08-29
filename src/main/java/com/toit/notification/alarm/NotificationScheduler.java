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
     * 알림 오프셋이 최대 10분 전이라, 10분을 넘기면 어떤 설정이든 일정이 이미
     * 시작한 뒤다. 회의 중에 오는 "5분 전입니다" 는 알림이 아니라 방해다.
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
            long alarmOffsetMinutes = alarm.getAlarmOffsetMinutes() != null ? alarm.getAlarmOffsetMinutes() : 0L;
            String body = "일정이 시작되기 " + alarmOffsetMinutes + "분 전입니다.";

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
