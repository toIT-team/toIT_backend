package com.toit.notification.push;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.toit.notification.push.FcmToken;
import com.toit.notification.push.FcmTokenRepository;
import com.toit.notification.push.request.FcmNotificationRequest;
import com.toit.user.Users;
import com.toit.user.settings.UsersSettings;
import com.toit.user.settings.UsersSettingsRepository;
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

    /** 성공 여부만 필요한 곳을 위한 짧은 형태 */
    public boolean sendToUser(Users user, FcmNotificationRequest request) {
        return send(user, request).isSent();
    }

    public boolean sendToUserIgnoringAppAlarmEnabled(Users user, FcmNotificationRequest request) {
        return sendToUserInternal(user, request, true).isSent();
    }

    /**
     * 스케줄러용. 왜 실패했는지까지 돌려준다.
     * 오류 코드를 모르면 재시도할지 토큰을 지울지 정할 수가 없다.
     */
    public FcmSendResult send(Users user, FcmNotificationRequest request) {
        return sendToUserInternal(user, request, false);
    }

    private FcmSendResult sendToUserInternal(Users user, FcmNotificationRequest request, boolean ignoreAppAlarmEnabled) {
        if (!ignoreAppAlarmEnabled) {
            UsersSettings usersSettings = usersSettingsRepository.findByUsers_UsersId(user.getUsersId());
            if (usersSettings == null || !Boolean.TRUE.equals(usersSettings.getAppAlarmEnabled())) {
                log.info("[FCM] 건너뜀 사유=앱알림꺼짐 usersId={}", user.getUsersId());
                return FcmSendResult.alarmOff();
            }
        }

        List<FcmToken> tokens = fcmTokenRepository.findAllByUsers(user);
        if (tokens.isEmpty()) {
            log.warn("[FCM] 건너뜀 사유=토큰없음 usersId={}", user.getUsersId());
            return FcmSendResult.noToken();
        }

        boolean isSent = false;
        String lastErrorCode = null;
        int deleted = 0;

        for (FcmToken fcmToken : tokens) {
            String errorCode = sendFcmMessage(fcmToken, request);
            if (errorCode == null) {
                isSent = true;
                continue;
            }
            lastErrorCode = errorCode;
            // 폐기된 토큰만 지운다. 몇 번을 보내도 살아나지 않는다.
            if (MessagingErrorCode.UNREGISTERED.name().equals(errorCode)) {
                log.warn("[FCM] 토큰제거 usersId={} token=...{}",
                        user.getUsersId(), shortenToken(fcmToken.getFcmToken()));
                fcmTokenRepository.delete(fcmToken);
                deleted++;
            }
        }

        // 하나라도 도착했으면 성공이다. 다른 기기 하나 때문에 재시도하면
        // 이미 받은 기기에서 한 번 더 울린다.
        if (isSent) {
            return FcmSendResult.sent();
        }
        // 토큰이 전부 폐기돼 보낼 데가 없으면 기다려도 달라지지 않는다.
        if (deleted == tokens.size()) {
            return FcmSendResult.noToken();
        }
        return FcmSendResult.failed(lastErrorCode);
    }

    /**
     * 한 토큰에 보낸다.
     *
     * @return 성공이면 null, 실패면 오류 코드. 코드조차 못 받았으면 TIMEOUT.
     */
    private String sendFcmMessage(FcmToken fcmTokenEntity, FcmNotificationRequest request) {
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
            return null;
        } catch (FirebaseMessagingException e) {
            // INVALID_ARGUMENT 는 토큰이 아니라 메시지 필드가 잘못돼도 나온다.
            // 여기서 토큰을 지우면 멀쩡한 토큰을 버리게 된다.
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            String code = errorCode == null ? FcmSendResult.TIMEOUT : errorCode.name();
            log.error("[FCM] 전송실패 usersId={} token=...{} errorCode={} message={}",
                    fcmTokenEntity.getUsers().getUsersId(),
                    shortenToken(fcmTokenEntity.getFcmToken()),
                    code,
                    e.getMessage());
            return code;
        } catch (Exception e) {
            // 타임아웃처럼 FCM 에 닿기 전에 끊긴 경우. 오류 코드 자체가 없다.
            log.error("[FCM] 예외 usersId={} token=...{} message={}",
                    fcmTokenEntity.getUsers().getUsersId(),
                    shortenToken(fcmTokenEntity.getFcmToken()),
                    e.getMessage());
            return FcmSendResult.TIMEOUT;
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
