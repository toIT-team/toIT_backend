package com.toit.auth.jwt;

import com.toit.common.UsersRole.UsersRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${JWT_SECRET}") String secret,
            @Value("${ACCESS_TOKEN_EXPIRATION}") long accessTokenExpiration,
            @Value("${REFRESH_TOKEN_EXPIRATION}") long refreshTokenExpiration
    ) {
        // .env에서 가져온 문자열 키를 HMAC-SHA 알고리즘용 객체로 변환합니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }


    /**
     * Refresh Token 생성: 유저 PK만 담아 가볍게 만듭니다.
     */
    public String createAccessToken(Long usersId, String email, String nickname, UsersRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        var builder = Jwts.builder()
                .subject(String.valueOf(usersId)) // 토큰의 주인 (유저 ID)
                .claim("role", role.name())      // ROLE_USER 또는 ROLE_ADMIN 저장
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey);            // 이미 생성자에서 만든 secretKey 사용

        if (email != null && !email.isBlank()) {
            builder.claim("email", email);
        }
        if (nickname != null && !nickname.isBlank()) {
            builder.claim("nickname", nickname);
        }

        return builder.compact();
    }

    /**
     *  Refresh Token 생성: 유저 PK만 담아 가볍게 만듭니다.
     * 보안을 위해 유저 정보(이메일, 권한 등)는 싣지 않는 것이 관례입니다.
     */
    public String createRefreshToken(Long usersId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration); // 긴 만료 시간 사용

        return Jwts.builder()
                .subject(String.valueOf(usersId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // JwtProvider.java에 추가
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 만료되었거나, 위조되었거나, 형식이 잘못된 경우
            return false;
        }
    }

    public String getUserId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class); // 클레임에서 "role" 키의 값을 가져옴
    }
}
