package com.toit.user;

import com.toit.folders.Folders;
import com.toit.schedules.Schedules;
import com.toit.common.enums.AuthProvider;
import com.toit.common.enums.EntityStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersId;

    /**
     * 사용자 이메일 - OAuth 2.0으로 가져온 값
     */
    @Column(nullable = false)
    private String email;

    /**
     * 사용자 이름 - OAuth 2.0으로 가져온 값
     */
    @Column(nullable = false)
    private String name;

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
    private AuthProvider authProvider;

    /**
     * 사용자 OAuth 식별자 - OAuth 2.0으로 가져온 값
     */
    private Long providerUsersId;

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

    /**
     * 스케줄(schedules)과 1:N 관계
     * 외래 키의 주인은 Schedules
     */
    //1:N 관계
    @OneToMany(mappedBy = "users")
    private List<Schedules> schedules = new ArrayList<>();

    public Users(String email, String name, String bio, AuthProvider authProvider, Long providerUsersId, LocalDateTime createdAt
    ) {
        this.email = email;
        this.name = name;
        this.bio = bio;
        this.authProvider = authProvider;
        this.providerUsersId = providerUsersId;
        this.status = EntityStatus.ACTIVE;
        this.createdAt = createdAt;
    }

}
