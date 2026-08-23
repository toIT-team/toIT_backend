package com.toit.view.pagenotifications.dto.response;

import com.toit.notification.inbox.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationViewResponse {

    private Long notificationId;
    private String title;
    private NotificationType type;
    private String deeplink;
    private LocalDateTime sentAt;
    private Boolean isRead;

    public NotificationViewResponse(Long notificationId, String title, NotificationType type, String deeplink,
                                    LocalDateTime sentAt, Boolean isRead) {
        this.notificationId = notificationId;
        this.title = title;
        this.type = type;
        this.deeplink = deeplink;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }
}
