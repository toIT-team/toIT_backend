package com.toit.auth;


import com.toit.auth.dto.request.RefreshTokenRequest;
import com.toit.auth.dto.response.AuthMeResponse;
import com.toit.auth.dto.response.RefreshTokenResponse;
import com.toit.common.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @Operation(summary = "로그인 상태/온보딩 확인",
            description = "액세스 토큰으로 현재 로그인한 사용자 정보를 조회합니다. "
                    + "needsNickname 이 true 이면 닉네임 미설정 신규 사용자이므로 닉네임 입력 화면으로 분기합니다.")
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(authService.getMe(usersId));
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

    @Operation(summary = "CloudFront Signed Cookie 발급 및 재발급", description = "이미지 조회에 필요한 CloudFront Signed Cookie를 발급합니다. 만료 시 동일 API로 재발급합니다.")
    @GetMapping("/cloudfront/cookie")
    public ResponseEntity<Map<String, String>> issueCloudFrontCookie() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(authService.issueCloudFrontCookies(usersId));
    }
}
