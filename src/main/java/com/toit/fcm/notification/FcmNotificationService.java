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

    public boolean sendToUser(Users user, FcmNotificationRequest request) {
        return sendToUserInternal(user, request, false);
    }

    public boolean sendToUserIgnoringAppAlarmEnabled(Users user, FcmNotificationRequest request) {
        return sendToUserInternal(user, request, true);
    }

    private boolean sendToUserInternal(Users user, FcmNotificationRequest request, boolean ignoreAppAlarmEnabled) {
        if (!ignoreAppAlarmEnabled) {
            UsersSettings usersSettings = usersSettingsRepository.findByUsers_UsersId(user.getUsersId());
            if (usersSettings == null || !Boolean.TRUE.equals(usersSettings.getAppAlarmEnabled())) {
                log.info("[FCM] 건너뜀 사유=앱알림꺼짐 usersId={}", user.getUsersId());
                return false;
            }
        }

        List<FcmToken> tokens = fcmTokenRepository.findAllByUsers(user);
        if (tokens.isEmpty()) {
            log.warn("[FCM] 건너뜀 사유=토큰없음 usersId={}", user.getUsersId());
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
            log.info("[FCM] 전송성공 usersId={} token=...{}",
                    fcmTokenEntity.getUsers().getUsersId(),
                    shortenToken(fcmTokenEntity.getFcmToken()));
            return true;
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("[FCM] 토큰제거 usersId={} token=...{} errorCode={}",
                        fcmTokenEntity.getUsers().getUsersId(),
                        shortenToken(fcmTokenEntity.getFcmToken()),
                        errorCode);
                fcmTokenRepository.delete(fcmTokenEntity);
            } else {
                log.error("[FCM] 전송실패 usersId={} token=...{} errorCode={} message={}",
                        fcmTokenEntity.getUsers().getUsersId(),
                        shortenToken(fcmTokenEntity.getFcmToken()),
                        errorCode,
                        e.getMessage());
            }
            return false;
        } catch (Exception e) {
            log.error("[FCM] 예외 usersId={} token=...{} message={}",
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
