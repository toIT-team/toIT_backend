package com.toit.view.pagenotifications;

import com.toit.view.pagenotifications.dto.response.PageNotificationsViewResponse;

public interface PageNotificationsUseCase {

    PageNotificationsViewResponse getNotificationsView(Long usersId);
}
