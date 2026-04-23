package com.toit.view.pagenotifications.dto.response;

import com.toit.notification.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationViewResponse {

    private Long notificationId;
    private String title;
    private NotificationType type;
    private String deeplink;
    private Boolean isRead;

    public NotificationViewResponse(Long notificationId, String title, NotificationType type, String deeplink, Boolean isRead) {
        this.notificationId = notificationId;
        this.title = title;
        this.type = type;
        this.deeplink = deeplink;
        this.isRead = isRead;
    }
}
