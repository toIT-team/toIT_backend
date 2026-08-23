package com.toit.notification.push.request;

public record FcmNotificationRequest(
        String title,
        String body,
        String type,
        String link,
        Long notificationId
) {
}
