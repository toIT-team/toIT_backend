package com.toit.auth;


import com.toit.auth.dto.request.RefreshTokenRequest;
import com.toit.auth.dto.response.RefreshTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/kakao/login")
    public ResponseEntity<Void> startKakaoLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/kakao")
                .build();
    }

    @GetMapping("/apple/login")
    public ResponseEntity<Void> startAppleLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/apple")
                .build();
    }

    /**
     * Access Token 재발급 API
     * 클라이언트가 만료된 Access Token 대신 Refresh Token을 보내면,
     * 서버가 검증 후 새로운 Access Token을 발급하여 응답합니다.
     */
    @PostMapping("/reissue")
    public ResponseEntity<RefreshTokenResponse> reissueAccessToken(@RequestBody RefreshTokenRequest request) {
        String newAccessToken = authService.reissueAccessToken(request.getRefreshToken());
        RefreshTokenResponse response = new RefreshTokenResponse(newAccessToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "탈퇴 계정 복구", description = "복구 토큰으로 탈퇴한 계정을 복구하고 로그인 토큰을 발급합니다.")
    @PostMapping("/restore")
    public ResponseEntity<AuthService.LoginTokens> restoreAccount(@RequestParam String restoreToken) {
        return ResponseEntity.ok(authService.restoreAccount(restoreToken));
    }
}
