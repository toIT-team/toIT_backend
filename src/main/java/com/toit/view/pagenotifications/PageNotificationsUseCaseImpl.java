package com.toit.view.pagenotifications;

import com.toit.notification.inbox.NotificationType;
import com.toit.notification.inbox.UserNotification;
import com.toit.notification.inbox.UserNotificationService;
import com.toit.view.pagenotifications.dto.response.NotificationViewResponse;
import com.toit.view.pagenotifications.dto.response.PageNotificationsViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageNotificationsUseCaseImpl implements PageNotificationsUseCase {

    private final UserNotificationService userNotificationService;

    @Override
    public PageNotificationsViewResponse getNotificationsView(Long usersId) {
        // 조회는 보낸 시각 최신순이고, 종류로 나눠도 그 순서가 유지된다.
        Map<NotificationType, List<NotificationViewResponse>> byType =
                userNotificationService.getNotifications(usersId).stream()
                        .collect(Collectors.groupingBy(
                                UserNotification::getType,
                                Collectors.mapping(this::toView, Collectors.toList())
                        ));

        return new PageNotificationsViewResponse(
                byType.getOrDefault(NotificationType.NOTICE, List.of()),
                byType.getOrDefault(NotificationType.FEEDBACK_REPLY, List.of()),
                byType.getOrDefault(NotificationType.SCHEDULE, List.of())
        );
    }

    private NotificationViewResponse toView(UserNotification notification) {
        return new NotificationViewResponse(
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getType(),
                notification.getDeeplink(),
                notification.getSentAt(),
                notification.getIsRead()
        );
    }
}
