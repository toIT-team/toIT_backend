package com.toit.auth.token;

import com.toit.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 1. 유저 객체(Users)로 기존 토큰 찾기
    Optional<RefreshToken> findByUsers(Users users);

    // 2. 토큰 문자열(refreshToken) 자체로 찾기
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}