package com.toit.notification.inbox;

import com.toit.user.Users;
import com.toit.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UsersService usersService;

    public UserNotification create(Users users, NotificationType type, String title, String deeplink, Long targetId) {
        UserNotification notification = new UserNotification(users, type, title, deeplink, targetId);
        return userNotificationRepository.save(notification);
    }

    /**
     * 멱등키로 찾고 없을 때만 만든다.
     *
     * 재시도할 때마다 알림함에 줄이 쌓이는 것을 막는다. 조회에서 걸러지므로 유니크
     * 제약은 스케줄러가 둘 이상일 때를 위한 안전망이고 지금은 걸리지 않는다.
     */
    public UserNotification findOrCreate(String idempotencyKey, Users users, NotificationType type,
                                         String title, String deeplink, Long targetId) {
        return userNotificationRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> userNotificationRepository.save(
                        new UserNotification(users, type, title, deeplink, targetId, idempotencyKey)));
    }

    public List<UserNotification> createAll(List<Users> users, NotificationType type, String title, String deeplink, Long targetId) {
        List<UserNotification> notifications = users.stream()
                .map(user -> new UserNotification(user, type, title, deeplink, targetId))
                .collect(Collectors.toList());

        return userNotificationRepository.saveAll(notifications);
    }

    /**
     * 푸시 없이 알림함에만 남긴다.
     *
     * 알림함 조회는 isSent = true 만 보므로, 만들면서 바로 보낸 것으로 표시해야
     * 목록에 뜬다. 여기서 "보냈다" 는 푸시가 나갔다는 뜻이 아니라 알림함에 놓았다는
     * 뜻이다. 공지 푸시를 되살리면 createAll 로 돌아간다.
     */
    public List<UserNotification> createAllAsSent(List<Users> users, NotificationType type,
                                                  String title, String deeplink, Long targetId) {
        List<UserNotification> notifications = users.stream()
                .map(user -> {
                    UserNotification notification =
                            new UserNotification(user, type, title, deeplink, targetId);
                    notification.markAsSent();
                    return notification;
                })
                .collect(Collectors.toList());

        return userNotificationRepository.saveAll(notifications);
    }

    public List<UserNotification> getNotifications(Long usersId) {
        usersService.findById(usersId);
        return userNotificationRepository.findAllByUsers_UsersIdAndIsSentTrueOrderBySentAtDesc(usersId);
    }

    public long getUnreadCount(Long usersId) {
        usersService.findById(usersId);
        return userNotificationRepository.countByUsers_UsersIdAndIsSentTrueAndIsReadFalse(usersId);
    }

    public void markAsSent(UserNotification notification) {
        notification.markAsSent();
        userNotificationRepository.save(notification);
    }

    public void markAsRead(Long usersId, Long notificationId) {
        UserNotification notification = userNotificationRepository.findByNotificationIdAndUsers_UsersId(notificationId, usersId)
                .orElseThrow(() -> new IllegalArgumentException("notificationId가 " + notificationId + "인 알림을 찾을 수 없습니다."));

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }

        notification.markAsRead();
        userNotificationRepository.save(notification);
    }
}
