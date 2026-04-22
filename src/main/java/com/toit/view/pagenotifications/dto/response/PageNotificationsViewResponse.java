package com.toit.view.pagenotifications.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageNotificationsViewResponse {

    private List<NotificationViewResponse> notifications;

    public PageNotificationsViewResponse(List<NotificationViewResponse> notifications) {
        this.notifications = notifications;
    }
}
