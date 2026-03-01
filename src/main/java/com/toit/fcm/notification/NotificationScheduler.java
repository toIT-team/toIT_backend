package com.toit.fcm.notification;

import com.google.firebase.messaging.*;
import com.toit.fcm.FcmToken;
import com.toit.fcm.FcmTokenRepository;
import com.toit.schedules.Schedules;
import com.toit.schedules.SchedulesRepository;
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

    private final SchedulesRepository schedulesRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final SchedulesAlarmRepository schedulesAlarmRepository;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendAlerts() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime nextMinute = now.plusMinutes(1);

        log.info("스케줄러 조회 시간: {}", now);

        // 보내야 할 알림만 가져옴
        List<SchedulesAlarm> alarms = schedulesAlarmRepository.findTargetAlarms(now, nextMinute);

        if (alarms.isEmpty()) return; // 보낼 게 없으면 종료

        log.info("알림 발송 작업 시작: 총 {}건 예정", alarms.size());

        for (SchedulesAlarm alarm : alarms) {
            Schedules schedule = alarm.getSchedules();
            Users user = schedule.getUsers();

            // 2. 유저의 FCM 토큰들 조회
            List<FcmToken> tokens = fcmTokenRepository.findAllByUsers(user);

            if (tokens.isEmpty()) {
                log.warn("사용자(ID={})의 FCM 토큰이 없습니다. 알림 건너뜀.", user.getUsersId());
                // 주의: 토큰이 없더라도 나중에 중복으로 찾지 않게 isSent 처리는 해주는 게 좋습니다.
                alarm.markAsSent();
                continue;
            }

            // 3. 알림 내용 구성
            String title = schedule.getTitle();
            String rawMemo = schedule.getMemo();
            String body = (rawMemo == null || rawMemo.isBlank()) ? "일정 시간이 되었습니다." :
                    (rawMemo.length() > 100 ? rawMemo.substring(0, 100) + "..." : rawMemo);

            // 4. 각 기기(토큰)별로 발송
            for (FcmToken fcmToken : tokens) {

                sendFcmMessage(fcmToken, title, body);
            }
            alarm.markAsSent();
        }
    }

    /**
     * 실제 FCM 전송 및 죽은 토큰 삭제 로직
     */
    private void sendFcmMessage(FcmToken fcmTokenEntity, String title, String body) {
        try {
            // 1. [추가] 안드로이드 설정: 중요도를 'HIGH'로 설정
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH) // 중요도: 높음
                    .setNotification(AndroidNotification.builder()
                            .setSound("default") // 소리도 나게 설정 (선택사항)
                            .build())
                    .build();


            Message message = Message.builder()
                    .setToken(fcmTokenEntity.getFcmToken()) // Entity 필드명 확인 (token인지 fcmToken인지)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(androidConfig)
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("전송 성공: UserID={}, Token=...{}", fcmTokenEntity.getUsers().getUsersId(), fcmTokenEntity.getFcmToken().substring(0, 10));

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("유효하지 않은 토큰 감지 및 삭제: {}", fcmTokenEntity.getFcmToken());
                fcmTokenRepository.delete(fcmTokenEntity); // DB에서 즉시 삭제해버린다.
            } else {
                log.error(" 전송 실패 (서버 에러): {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("알 수 없는 에러: {}", e.getMessage());
        }
    }
}