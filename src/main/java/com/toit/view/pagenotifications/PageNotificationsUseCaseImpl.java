package com.toit.view.pagenotifications;

import com.toit.notification.UserNotification;
import com.toit.notification.UserNotificationService;
import com.toit.view.pagenotifications.dto.response.NotificationViewResponse;
import com.toit.view.pagenotifications.dto.response.PageNotificationsViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageNotificationsUseCaseImpl implements PageNotificationsUseCase {

    private final UserNotificationService userNotificationService;

    @Override
    public PageNotificationsViewResponse getNotificationsView(Long usersId) {
        List<UserNotification> notifications = userNotificationService.getNotifications(usersId);

        List<NotificationViewResponse> responses = notifications.stream()
                .map(notification -> new NotificationViewResponse(
                        notification.getNotificationId(),
                        notification.getTitle(),
                        notification.getType(),
                        notification.getDeeplink(),
                        notification.getIsRead()
                ))
                .collect(Collectors.toList());

        return new PageNotificationsViewResponse(responses);
    }
}
