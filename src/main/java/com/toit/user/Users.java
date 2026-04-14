package com.toit.user;


import com.toit.common.UsersRole.UsersRole;
import com.toit.common.enums.AuthProvider;
import com.toit.common.enums.EntityStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_provider_identity",
                        columnNames = {"auth_provider", "provider_users_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class Users {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersId;

    /**
     * 사용자 이메일 - OAuth 2.0으로 가져온 값
     */
    @Column
    private String email;

    /**
     * 사용자 이름 - OAuth 2.0으로 가져온 값
     */
    @Column(nullable = false)
    private String name;

    /***
     * 유저의 권한(사용자,관리자 )
     * 임시로 USER 지정
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsersRole role = UsersRole.ROLE_USER;

    /***
     * 사용자의 프로필 이미지
     */
    @Column
    private String imageUrl;

    /**
     * 사용자 간단 자기소개
     * 길이 제한 100
     */
    @Column(nullable = true, length = 100)
    private String bio;

    /**
     * 사용자 OAuth 종류
     * ENUM형태 ("GOOGLE'", "KAKAO")
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    /**
     * 사용자 OAuth 식별자 - OAuth 2.0으로 가져온 값
     */
    @Column(name = "provider_users_id", nullable = false)
    private String providerUsersId;

    /**
     * 사용자 서비스 상태
     * ENUM 형태(ACTIVE, DELETED)
     * ACTIVITY일 경우 활성화, DELETED 경우 비홣성화
     */
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    /**
     * 사용자 생성 후 시간 즉시 생성
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 사용자 서비스 상태가 DELETED 되면 업데이트
     */
    @Column(nullable = true)
    private LocalDateTime deletedAt;


    public Users(String email, String name, String bio, String imageUrl, AuthProvider authProvider, String providerUsersId, LocalDateTime createdAt) {
        this.email = email;
        this.name = name;
        this.bio = bio;
        this.imageUrl = imageUrl;
        this.authProvider = authProvider;
        this.providerUsersId = providerUsersId;
        this.status = EntityStatus.ACTIVE;
        this.createdAt = createdAt;
    }

    /**
     * 이름 업데이트 메서드 추가
     */
    // 업데이트 메서드 확장 (더티 체킹용)
    public void updateSocialProfile(String email, String name, String imageUrl) {
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
    }

    public boolean requiresAdditionalInfo() {
        return email == null || email.isBlank();
    }

    public boolean isDeleted() {
        return status == EntityStatus.DELETED;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void restore() {
        this.status = EntityStatus.ACTIVE;
        this.deletedAt = null;
    }

    public void softDelete() {
        this.status = EntityStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
