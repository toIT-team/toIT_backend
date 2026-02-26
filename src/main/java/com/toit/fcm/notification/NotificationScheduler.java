package com.toit.fcm.notification;

import com.google.firebase.messaging.*;
import com.toit.fcm.FcmToken;
import com.toit.fcm.FcmTokenRepository;
import com.toit.schedules.Schedules;
import com.toit.schedules.SchedulesRepository;
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

    /**
     * 1분마다 실행 (초, 분, 시, 일, 월, 요일)
     * 예: 14:00:00, 14:01:00 ...
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendAlerts() {
        // 현재 시간의 '분(Minute)' 범위를 구한다. (예: 14:00:00 ~ 14:00:59)
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime nextMinute = now.plusMinutes(1);
        log.info(" 스케줄러 조회 시간: {}", now);
        List<Schedules> schedules = schedulesRepository.findTargetSchedules(now, nextMinute);

        if (schedules.isEmpty()) return; // 보낼 게 없으면 종료

        log.info("알림 발송 작업 시작: 총 {}건 예정", schedules.size());

        for (Schedules schedule : schedules) {
            //  해당 유저의 모든 토큰을 가져온다. (폰, 태블릿 등 여러 기기일 수 있음)
            // 주의: FcmTokenRepository에 'findAllByUsers' 메서드가 있어야 한다.
            List<FcmToken> tokens = fcmTokenRepository.findAllByUsers(schedule.getUsers());

            if (tokens.isEmpty()) {
                log.warn("사용자(ID={})의 FCM 토큰이 없습니다. 알림 건너뜀.", schedule.getUsers().getUsersId());
                continue;
            }

            //  알림 내용 구성
            String title = schedule.getTitle();
            String rawMemo = schedule.getMemo();
            String body;

            if (rawMemo == null || rawMemo.isBlank()) {
                body = "일정 시간이 되었습니다.";
            } else {
                // 메모가 100자보다 길면 자르고 "..." 추가
                body = (rawMemo.length() > 100) ? rawMemo.substring(0, 100) + "..." : rawMemo;
            }


            //  각 토큰으로 알림 전송
            for (FcmToken fcmToken : tokens) {
                sendFcmMessage(fcmToken, title, body);
            }

            // 발송 성공 후 상태 변경
            schedule.markAsSent();
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