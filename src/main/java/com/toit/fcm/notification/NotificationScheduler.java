package com.toit.fcm.notification;

import com.toit.schedules.Schedules;
import com.toit.schedules.SchedulesRepository;
import com.toit.schedulesalarm.SchedulesAlarm;
import com.toit.schedulesalarm.SchedulesAlarmRepository;
import com.toit.fcm.request.FcmNotificationRequest;
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

    private final SchedulesRepository schedulesRepository;
    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final FcmNotificationService fcmNotificationService;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendAlerts() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime nextMinute = now.plusMinutes(1);

        log.info("스케줄러 조회 시간: {}", now);

        // 보내야 할 알림만 가져옴
        List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, nextMinute);
        //
        if (alarms.isEmpty()) return; // 보낼 게 없으면 종료

        log.info("알림 발송 작업 시작: 총 {}건 예정", alarms.size());

        for (SchedulesAlarm alarm : alarms) {
            Schedules schedule = alarm.getSchedules();
            Users user = schedule.getUsers();

            // 3. 알림 내용 구성
            String title = schedule.getTitle();
            String rawMemo = schedule.getMemo();
            String body = (rawMemo == null || rawMemo.isBlank()) ? "일정 시간이 되었습니다." :
                    (rawMemo.length() > 100 ? rawMemo.substring(0, 100) + "..." : rawMemo);

            // 4. 각 기기(토큰)별로 발송
            fcmNotificationService.sendToUser(
                    user,
                    new FcmNotificationRequest(
                            title,
                            body,
                            "schedule_detail",
                            "toit://schedule?id=" + schedule.getSchedulesId()
                    )
            );
            alarm.markAsSent();
        }
    }
}
