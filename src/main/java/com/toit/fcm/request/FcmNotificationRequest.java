package com.toit.fcm.request;

public record FcmNotificationRequest(
        String title,
        String body,
        String type,
        String link,
        Long notificationId
) {
}
