package com.toit.auth.token;

import com.toit.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 생성/수정 시간 자동 기록
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 유저의 토큰인지 식별하는 ID (Users 테이블의 PK)
     * unique = true : 한 유저당 1개의 Refresh Token만 가지도록 강제 (보안 및 관리 용이)
     */
    @OneToOne(fetch = FetchType.LAZY) // 지연 로딩 필수!
    @JoinColumn(name = "users_id", nullable = false, unique = true)
    private Users users;
    /**
     * 실제 Refresh Token 문자열
     * JWT 토큰은 길이가 길어질 수 있으므로 넉넉하게 512자 할당
     */
    @Column(nullable = false, length = 512)
    private String refreshToken;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public RefreshToken(Users users, String refreshToken) {
        this.users = users;
        this.refreshToken = refreshToken;
    }

    // 업데이트 메서드도 변경된 필드명을 쓰도록 수정
    public void updateToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}