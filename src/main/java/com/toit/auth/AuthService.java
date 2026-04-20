package com.toit.auth;

import com.toit.auth.jwt.JwtProvider;
import com.toit.auth.token.RefreshToken;
import com.toit.auth.token.RefreshTokenRepository;
import com.toit.exception.auth.InvalidTokenException;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsersRepository usersRepository;

    /**
     * 로그인 성공 시 우리 서비스의 Access / Refresh Token을 발급하고,
     * 사용자당 1개의 Refresh Token만 유지되도록 저장 또는 갱신합니다.
     */
    @Transactional
    public LoginTokens issueLoginTokens(Users user) {
        String accessToken = jwtProvider.createAccessToken(
                user.getUsersId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getUsersId());

        refreshTokenRepository.findByUsers(user)
                .ifPresentOrElse(
                        tokenEntity -> tokenEntity.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .users(user)
                                        .refreshToken(refreshToken)
                                        .build()
                        )
                );

        return new LoginTokens(accessToken, refreshToken);
    }

    /**
     * 클라이언트로부터 받은 Refresh Token을 검증하고,
     * 유효하다면 새로운 Access Token을 발급하여 반환합니다.
     */
    @Transactional(readOnly = true)
    public String reissueAccessToken(String refreshToken) {

        // 1. 넘어온 Refresh Token 자체가 유효한지 검사 (만료, 위조 여부 확인)
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않거나 만료된 Refresh Token 입니다. 다시 로그인해주세요.");
        }

        // 2. DB에 해당 Refresh Token이 존재하는지 확인 (강제 로그아웃 또는 다른 기기 로그인으로 인해 삭제되었는지 검증)
        RefreshToken tokenEntity = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("DB에 존재하지 않는 토큰입니다. 다시 로그인해주세요."));

        // 3. 토큰의 주인(Users) 정보를 가져옵니다.
        Users user = tokenEntity.getUsers();

        // 4. 주인의 정보(id, email, role)를 담아 새로운 Access Token을 생성하여 반환합니다.
        return jwtProvider.createAccessToken(user.getUsersId(), user.getEmail(), user.getName(), user.getRole());
    }

    /**
     * 복구 토큰을 검증하고 계정을 복구한 뒤 로그인 토큰을 발급합니다.
     */
    @Transactional
    public LoginTokens restoreAccount(String restoreToken) {
        if (!jwtProvider.validateToken(restoreToken) || !jwtProvider.isRestoreToken(restoreToken)) {
            throw new RuntimeException("유효하지 않은 복구 토큰입니다.");
        }

        Long usersId = Long.parseLong(jwtProvider.getUserId(restoreToken));
        Users user = usersRepository.findById(usersId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!user.isDeleted()) {
            throw new RuntimeException("탈퇴한 계정이 아닙니다.");
        }

        user.restore();
        return issueLoginTokens(user);
    }

    public record LoginTokens(String accessToken, String refreshToken) {
    }
}
