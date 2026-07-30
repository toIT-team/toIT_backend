package com.toit.user;

import com.toit.common.enums.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 탈퇴 통계용 기록.
 * 사용자 레코드는 탈퇴 시 완전히 삭제되므로, 집계에 필요한 최소 정보만 남긴다.
 * 개인 식별 정보(이메일, 이름, 소셜 식별자)는 저장하지 않는다.
 */
@Entity
@Table(name = "withdrawal_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdrawalLogId;

    /**
     * 탈퇴한 사용자의 식별자.
     * 사용자 레코드가 삭제되므로 FK가 아닌 단순 값으로 보관한다.
     */
    @Column(nullable = false)
    private Long usersId;

    /**
     * 가입 경로 (KAKAO, APPLE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    /**
     * 가입 시각 - 탈퇴까지 걸린 기간 집계용
     */
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private LocalDateTime withdrawnAt;

    public WithdrawalLog(Users user) {
        this.usersId = user.getUsersId();
        this.authProvider = user.getAuthProvider();
        this.joinedAt = user.getCreatedAt();
        this.withdrawnAt = LocalDateTime.now();
    }
}
