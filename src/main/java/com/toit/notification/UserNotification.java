package com.toit.notification;

import com.toit.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 500)
    private String deeplink;

    @Column
    private Long targetId;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private Boolean isSent;

    @Column
    private LocalDateTime sentAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserNotification(Users users, NotificationType type, String title, String deeplink, Long targetId) {
        this.users = users;
        this.type = type;
        this.title = title;
        this.deeplink = deeplink;
        this.targetId = targetId;
        this.isRead = false;
        this.isSent = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
