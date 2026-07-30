package com.toit.auth.token;

import com.toit.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 1. 유저 객체(Users)로 기존 토큰 찾기
    Optional<RefreshToken> findByUsers(Users users);

    // 2. 토큰 문자열(refreshToken) 자체로 찾기
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    /**
     * 회원 탈퇴용 - 삭제 시 재발급(reissue)이 차단된다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken r where r.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
