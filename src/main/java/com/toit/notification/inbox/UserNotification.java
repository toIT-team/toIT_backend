package com.toit.notification.inbox;

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

    /**
     * 같은 알림을 두 번 적지 않기 위한 키.
     *
     * 알림함은 일정 알림만 담는 테이블이 아니라서 종류마다 구분하는 값이 다르다.
     * 컬럼을 종류별로 늘리는 대신 문자열 하나에 담는다.
     *   alarm:{예약번호}:{울릴시각}   notice:{공지번호}   feedback:{피드백번호}
     */
    @Column(unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private Boolean isSent;

    @Column
    private LocalDateTime sentAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserNotification(Users users, NotificationType type, String title, String deeplink, Long targetId) {
        this(users, type, title, deeplink, targetId, null);
    }

    public UserNotification(Users users, NotificationType type, String title, String deeplink,
                            Long targetId, String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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
