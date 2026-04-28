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

        log.info("스케줄러 조회 시간: {}", now);

        List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, nextMinute);
        if (alarms.isEmpty()) return;

        log.info("알림 발송 작업 시작: 총 {}건 예정", alarms.size());

        for (SchedulesAlarm alarm : alarms) {
            Schedules schedule = alarm.getSchedules();
            Users user = schedule.getUsers();

            String title = schedule.getTitle();
            String rawMemo = schedule.getMemo();
            String body = (rawMemo == null || rawMemo.isBlank()) ? "일정 시간이 도착했습니다." :
                    (rawMemo.length() > 100 ? rawMemo.substring(0, 100) + "..." : rawMemo);

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
                            notification.getDeeplink()
                    )
            );

            if (isSent) {
                userNotificationService.markAsSent(notification);
            }

            alarm.markAsSent();
            schedulesAlarmRepository.save(alarm);
        }
    }
}
