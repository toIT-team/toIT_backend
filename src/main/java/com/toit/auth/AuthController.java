package com.toit.auth;


import com.toit.auth.dto.request.RefreshTokenRequest;
import com.toit.auth.dto.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/social/kakao/login")
    public ResponseEntity<Void> startKakaoLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/kakao")
                .build();
    }

    /**
     * Access Token 재발급 API
     * 클라이언트가 만료된 Access Token 대신 Refresh Token을 보내면,
     * 서버가 검증 후 새로운 Access Token을 발급하여 응답합니다.
     */
    @PostMapping("/reissue")
    public ResponseEntity<RefreshTokenResponse> reissueAccessToken(@RequestBody RefreshTokenRequest request) {

        // 1. 서비스 계층에 토큰 검증 및 재발급 로직 위임
        String newAccessToken = authService.reissueAccessToken(request.getRefreshToken());

        // 2. 발급된 새 Access Token을 응답 DTO에 담아 200 OK 상태로 반환
        RefreshTokenResponse response = new RefreshTokenResponse(newAccessToken);
        return ResponseEntity.ok(response);
    }
}
