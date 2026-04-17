package com.toit.fcm.notification;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
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

    // 사용자 알림 설정을 확인한 뒤 등록된 모든 FCM 토큰으로 알림을 보낸다.
    public void sendToUser(Users user, FcmNotificationRequest request) {
        UsersSettings usersSettings = usersSettingsRepository.findByUsers_UsersId(user.getUsersId());
        if (usersSettings == null || !Boolean.TRUE.equals(usersSettings.getAppAlarmEnabled())) {
            log.info("사용자(ID={})의 앱 알림이 비활성화되어 있어 FCM 발송을 건너뜁니다.", user.getUsersId());
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findAllByUsers(user);
        if (tokens.isEmpty()) {
            log.warn("사용자(ID={})의 FCM 토큰이 없습니다. 알림 건너뜀.", user.getUsersId());
            return;
        }

        for (FcmToken fcmToken : tokens) {
            sendFcmMessage(fcmToken, request);
        }
    }

    // 개별 토큰에 대해 Firebase 메시지를 구성하고 실제 전송한다.
    private void sendFcmMessage(FcmToken fcmTokenEntity, FcmNotificationRequest request) {
        try {
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
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
                    .setAndroidConfig(androidConfig);

            if (request.link() != null && !request.link().isBlank()) {
                messageBuilder.putData("link", request.link());
            }

            Message firebaseMessage = messageBuilder.build();

            FirebaseMessaging.getInstance().send(firebaseMessage);
            log.info("전송 성공: UserID={}, Token=...{}",
                    fcmTokenEntity.getUsers().getUsersId(),
                    shortenToken(fcmTokenEntity.getFcmToken()));
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("유효하지 않은 토큰 감지 및 삭제: {}", fcmTokenEntity.getFcmToken());
                fcmTokenRepository.delete(fcmTokenEntity);
            } else {
                log.error("전송 실패 (서버 에러): {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("알 수 없는 에러: {}", e.getMessage());
        }
    }

    // 본문이 비었을 때 기본 문구를 넣고, 너무 길면 잘라낸다.
    private String normalizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "알림이 도착했습니다.";
        }
        return body.length() > 100 ? body.substring(0, 100) + "..." : body;
    }

    // 로그에 토큰 전체를 남기지 않도록 앞부분만 반환한다.
    private String shortenToken(String token) {
        if (token == null || token.length() <= 10) {
            return token;
        }
        return token.substring(0, 10);
    }
}
