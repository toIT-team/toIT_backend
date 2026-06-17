package com.toit.fcm.notification;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.toit.fcm.FcmToken;
import com.toit.fcm.FcmTokenRepository;
import com.toit.fcm.request.FcmNotificationRequest;
import com.toit.user.Users;
import com.toit.usersinfo.UsersSettings;
import com.toit.usersinfo.UsersSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UsersSettingsRepository usersSettingsRepository;

    // [FCM 비활성화] 발송 중단 — 호출부는 false를 받아 markAsSent를 건너뛴다(알림 DB 기록은 유지).
    public boolean sendToUser(Users user, FcmNotificationRequest request) {
        log.info("[FCM 비활성화] sendToUser 건너뜀. usersId={}", user.getUsersId());
        return false;
        // return sendToUserInternal(user, request, false);
    }

    public boolean sendToUserIgnoringAppAlarmEnabled(Users user, FcmNotificationRequest request) {
        log.info("[FCM 비활성화] sendToUserIgnoringAppAlarmEnabled 건너뜀. usersId={}", user.getUsersId());
        return false;
        // return sendToUserInternal(user, request, true);
    }

    private boolean sendToUserInternal(Users user, FcmNotificationRequest request, boolean ignoreAppAlarmEnabled) {
        if (!ignoreAppAlarmEnabled) {
            UsersSettings usersSettings = usersSettingsRepository.findByUsers_UsersId(user.getUsersId());
            if (usersSettings == null || !Boolean.TRUE.equals(usersSettings.getAppAlarmEnabled())) {
                log.info("사용자 앱 알림이 비활성화되어 FCM 발송을 건너뜁니다. usersId={}", user.getUsersId());
                return false;
            }
        }

        List<FcmToken> tokens = fcmTokenRepository.findAllByUsers(user);
        if (tokens.isEmpty()) {
            log.warn("사용자의 FCM 토큰이 없어 발송을 건너뜁니다. usersId={}", user.getUsersId());
            return false;
        }

        boolean isSent = false;
        for (FcmToken fcmToken : tokens) {
            if (sendFcmMessage(fcmToken, request)) {
                isSent = true;
            }
        }
        return isSent;
    }

    private boolean sendFcmMessage(FcmToken fcmTokenEntity, FcmNotificationRequest request) {
        try {
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .build())
                    .build();

            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .putHeader("apns-priority", "10")
                    .setAps(Aps.builder()
                            .setSound("default")
                            .build())
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmTokenEntity.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(normalizeBody(request.body()))
                            .build())
                    .putData("type", request.type())
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig);

            if (request.link() != null && !request.link().isBlank()) {
                messageBuilder.putData("link", request.link());
            }

            if (request.notificationId() != null) {
                messageBuilder.putData("notificationId", String.valueOf(request.notificationId()));
            }

            Message firebaseMessage = messageBuilder.build();

            FirebaseMessaging.getInstance().send(firebaseMessage);
            log.info("FCM 전송 성공: usersId={}, token=...{}",
                    fcmTokenEntity.getUsers().getUsersId(),
                    shortenToken(fcmTokenEntity.getFcmToken()));
            return true;
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("유효하지 않은 FCM 토큰을 제거합니다. usersId={}, token=...{}, errorCode={}",
                        fcmTokenEntity.getUsers().getUsersId(),
                        shortenToken(fcmTokenEntity.getFcmToken()),
                        errorCode);
                fcmTokenRepository.delete(fcmTokenEntity);
            } else {
                log.error("FCM 전송 실패: usersId={}, token=...{}, errorCode={}, message={}",
                        fcmTokenEntity.getUsers().getUsersId(),
                        shortenToken(fcmTokenEntity.getFcmToken()),
                        errorCode,
                        e.getMessage());
            }
            return false;
        } catch (Exception e) {
            log.error("FCM 전송 중 예상하지 못한 오류가 발생했습니다. usersId={}, token=...{}, message={}",
                    fcmTokenEntity.getUsers().getUsersId(),
                    shortenToken(fcmTokenEntity.getFcmToken()),
                    e.getMessage());
            return false;
        }
    }

    private String normalizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "알림이 도착했습니다.";
        }
        return body.length() > 100 ? body.substring(0, 100) + "..." : body;
    }

    private String shortenToken(String token) {
        if (token == null || token.length() <= 10) {
            return token;
        }
        return token.substring(0, 10);
    }
}
