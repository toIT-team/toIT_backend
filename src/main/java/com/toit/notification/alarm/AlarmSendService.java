package com.toit.notification.alarm;

import com.toit.notification.inbox.NotificationType;
import com.toit.notification.inbox.UserNotification;
import com.toit.notification.inbox.UserNotificationService;
import com.toit.notification.push.FcmNotificationService;
import com.toit.notification.push.FcmSendResult;
import com.toit.notification.push.request.FcmNotificationRequest;
import com.toit.schedules.Schedules;
import com.toit.user.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import io.micrometer.core.instrument.MeterRegistry;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSendService {
    /** 최초 발송 뒤 몇 번까지 다시 보낼지. 3번을 쓰면 1+2+4 로 7분을 덮는다. */
    private static final int MAX_ATTEMPT = 3;
    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final FcmNotificationService fcmNotificationService;
    private final UserNotificationService userNotificationService;
    private final MeterRegistry meterRegistry;

    /**
     * 한 건이 터져도 나머지 스레드는 계속 간다.
     *
     * 예전에는 루프 안에서 예외가 나면 그 실행이 통째로 끝났다. 2,500건짜리 배치의
     * 1,000번째가 터지면 나머지 1,500건이 그 실행에서 버려졌다.
     */
    public void sendOne(Long alarmId, LocalDateTime now){
        try{
            send(alarmId, now);
        }
        catch(Exception e){
            log.error("[ALARM] 발송실패 alarmId={}", alarmId, e);
        }
    }

    private void send(Long alarmId, LocalDateTime now){
        SchedulesAlarm alarm = schedulesAlarmRepository.findByIdWithSchedule(alarmId).orElse(null);
        // 조회한 뒤 지워졌을 수도 있다. 밀린 배치라면 그 사이가 몇 분씩 벌어진다.
        if (alarm == null){
            return ;
        }

        Schedules schedule = alarm.getSchedules();
        Users user = schedule.getUsers();
        String title = schedule.getTitle();
        String body = bodyOf(schedule, now);
        log.info("[ALARM] 발송시도 alarmId={} usersId={} scheduleId={} 예정={} 재시도={}",
                alarm.getSchedulesAlarmId(), user.getUsersId(),
                schedule.getSchedulesId(), alarm.getAlarmDateTime(), alarm.getAttemptCount());

        // 멱등키를 만들고 나서 진행하니 그 키를 찾아보고 있으면 쓰고 없으면 새로 만든다.
        //만약 사용자가 일정을 옮기면 alarm_date_time이 바뀐다. 그래서 이건 새줄로 진행
        // 만약 사용자가 알림을 설정하고 재시도 진입 시에 알림을 수정을 하면 재시도는 일어나지 않는다. 새 행이 생기는 게 아니라 이 행에 덮어씌어짐.
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
        // TODO 읽기와 저장 사이 330ms 동안 사용자가 일정을 수정하면 이 save 가
        //      그 수정을 덮어쓴다. @Version 이 없어 DB 도 안 막아준다.
        //      동시성을 올리면 부딪힐 창이 그만큼 늘어난다.
        //      docs/problems/notification-scheduler/throughput/lost-update.md
        schedulesAlarmRepository.save(alarm);

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
                meterRegistry.counter("alarm.sent").increment();
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
