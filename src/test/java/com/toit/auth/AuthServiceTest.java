package com.toit.auth;

import com.toit.auth.jwt.JwtProvider;
import com.toit.auth.token.RefreshToken;
import com.toit.auth.token.RefreshTokenRepository;
import com.toit.common.enums.AuthProvider;
import com.toit.user.Users;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void issueLoginTokensSavesNewRefreshToken() {
        Users user = new Users(
                "user@example.com",
                "tester",
                null,
                null,
                AuthProvider.KAKAO,
                "1000",
                LocalDateTime.now()
        );

        when(refreshTokenRepository.findByUsers(user)).thenReturn(Optional.empty());
        when(jwtProvider.createAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(any())).thenReturn("refresh-token");

        AuthService.LoginTokens tokens = authService.issueLoginTokens(user);

        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());
        verify(refreshTokenRepository).save(ArgumentMatchers.any(RefreshToken.class));
    }

    @Test
    void issueLoginTokensUpdatesExistingRefreshToken() {
        Users user = new Users(
                "user@example.com",
                "tester",
                null,
                null,
                AuthProvider.KAKAO,
                "1000",
                LocalDateTime.now()
        );
        RefreshToken refreshToken = RefreshToken.builder()
                .users(user)
                .refreshToken("old-refresh-token")
                .build();

        when(refreshTokenRepository.findByUsers(user)).thenReturn(Optional.of(refreshToken));
        when(jwtProvider.createAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(any())).thenReturn("new-refresh-token");

        AuthService.LoginTokens tokens = authService.issueLoginTokens(user);

        assertEquals("access-token", tokens.accessToken());
        assertEquals("new-refresh-token", tokens.refreshToken());
        assertEquals("new-refresh-token", refreshToken.getRefreshToken());
    }

    @Test
    void reissueAccessTokenReturnsNewAccessTokenForValidRefreshToken() {
        Users user = new Users(
                "user@example.com",
                "tester",
                null,
                null,
                AuthProvider.KAKAO,
                "1000",
                LocalDateTime.now()
        );
        RefreshToken refreshToken = RefreshToken.builder()
                .users(user)
                .refreshToken("valid-refresh-token")
                .build();

        when(jwtProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtProvider.createAccessToken(any(), any(), any(), any())).thenReturn("reissued-access-token");

        String accessToken = authService.reissueAccessToken("valid-refresh-token");

        assertEquals("reissued-access-token", accessToken);
    }
}

