package com.toit.notification;

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

    public List<UserNotification> createAll(List<Users> users, NotificationType type, String title, String deeplink, Long targetId) {
        List<UserNotification> notifications = users.stream()
                .map(user -> new UserNotification(user, type, title, deeplink, targetId))
                .collect(Collectors.toList());

        return userNotificationRepository.saveAll(notifications);
    }

    public List<UserNotification> getNotifications(Long usersId) {
        usersService.findById(usersId);
        return userNotificationRepository.findAllByUsers_UsersIdOrderByCreatedAtDesc(usersId);
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
