package com.toit.fcm;

import com.toit.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User와 N:1 관계 설정 (여러 토큰이 한 명의 유저에게 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    @Column(nullable = false, unique = true) // 토큰값은 중복되면 안 됨
    private String fcmToken;

    private LocalDateTime lastUpdatedAt;

    public FcmToken(Users users, String fcmToken) {
        this.users = users;
        this.fcmToken = fcmToken;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // 토큰 갱신 메서드 (Business Logic)
    // - 이미 있는 토큰인데 로그인했을 때 날짜만 갱신해줌
    public void updateTokenTimestamp() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}