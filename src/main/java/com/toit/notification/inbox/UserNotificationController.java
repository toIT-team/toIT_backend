package com.toit.notification.inbox;

import com.toit.common.SecurityUtil;
import com.toit.notification.inbox.dto.response.NotificationUnreadCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @Operation(summary = "확인하지 않은 알림 수 조회 API", description = "사용자의 미확인 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadCountResponse> getUnreadCount() {
        Long usersId = SecurityUtil.getCurrentUserId();
        long unreadCount = userNotificationService.getUnreadCount(usersId);
        return ResponseEntity.ok(new NotificationUnreadCountResponse(unreadCount));
    }

    @Operation(summary = "알림 읽음 처리", description = "사용자의 알림 1건을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        Long usersId = SecurityUtil.getCurrentUserId();
        userNotificationService.markAsRead(usersId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
