package com.toit.view.pagenotifications;

import com.toit.common.SecurityUtil;
import com.toit.view.pagenotifications.dto.response.PageNotificationsViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Notifications", description = "알림 화면 전용 API")
@RestController
@RequestMapping("/page/notifications")
@RequiredArgsConstructor
public class PageNotificationsViewController {

    private final PageNotificationsUseCase pageNotificationsUseCase;

    @Operation(summary = "알림 화면 API - 화면이름 : 알림")
    @GetMapping
    public ResponseEntity<PageNotificationsViewResponse> getNotifications() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(pageNotificationsUseCase.getNotificationsView(usersId));
    }
}
