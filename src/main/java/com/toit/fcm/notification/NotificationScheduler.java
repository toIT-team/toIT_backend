package com.toit.fcm.notification;

import com.toit.fcm.request.FcmNotificationRequest;
import com.toit.notification.NotificationType;
import com.toit.notification.UserNotification;
import com.toit.notification.UserNotificationService;
import com.toit.schedules.Schedules;
import com.toit.schedulesalarm.SchedulesAlarm;
import com.toit.schedulesalarm.SchedulesAlarmRepository;
import com.toit.user.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final FcmNotificationService fcmNotificationService;
    private final UserNotificationService userNotificationService;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendAlerts() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime nextMinute = now.plusMinutes(1);

        List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, nextMinute);

        log.info("[ALARM] 조회 window={}~{} count={}", now, nextMinute, alarms.size());
        if (alarms.isEmpty()) return;

        for (SchedulesAlarm alarm : alarms) {
            Schedules schedule = alarm.getSchedules();
            Users user = schedule.getUsers();

            String title = schedule.getTitle();
            long alarmOffsetMinutes = alarm.getAlarmOffsetMinutes() != null ? alarm.getAlarmOffsetMinutes() : 0L;
            String body = "일정이 시작되기 " + alarmOffsetMinutes + "분 전입니다.";

            log.info("[ALARM] 발송시도 alarmId={} usersId={} scheduleId={} 예정={}",
                    alarm.getSchedulesAlarmId(), user.getUsersId(),
                    schedule.getSchedulesId(), alarm.getAlarmDateTime());

            UserNotification notification = userNotificationService.create(
                    user,
                    NotificationType.SCHEDULE,
                    title,
                    "toit://schedule?id=" + schedule.getSchedulesId(),
                    schedule.getSchedulesId()
            );

            boolean isSent = fcmNotificationService.sendToUser(
                    user,
                    new FcmNotificationRequest(
                            title,
                            body,
                            "schedule_detail",
                            notification.getDeeplink(),
                            notification.getNotificationId()
                    )
            );

            if (isSent) {
                userNotificationService.markAsSent(notification);
                log.info("[ALARM] 발송성공 alarmId={} notificationId={}",
                        alarm.getSchedulesAlarmId(), notification.getNotificationId());
            } else {
                log.warn("[ALARM] 발송실패 alarmId={} notificationId={}",
                        alarm.getSchedulesAlarmId(), notification.getNotificationId());
            }

            alarm.markAsSent();
            schedulesAlarmRepository.save(alarm);

            // 발송 성공 여부와 무관하게 완료 처리된다. isSent=false 인 줄이 곧 유실이다.
            log.info("[ALARM] 완료표시 alarmId={} isSent={}",
                    alarm.getSchedulesAlarmId(), isSent);
        }
    }
}
