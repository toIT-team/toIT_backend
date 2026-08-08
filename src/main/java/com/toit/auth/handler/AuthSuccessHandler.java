package com.toit.auth.handler;



import com.toit.auth.AuthService;
import com.toit.auth.login.SocialLoginResult;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final UsersRepository usersRepository;

    @Value("${toit.auth.app-callback-uri:toit://auth/callback}")
    private String appCallbackUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = extractUserId(oAuth2User.getAttribute("toitUserId"));
        SocialLoginResult loginResult = SocialLoginResult.fromName(oAuth2User.getAttribute("toitLoginResult"));
        UriComponentsBuilder targetUrlBuilder = UriComponentsBuilder.fromUriString(appCallbackUri)
                .queryParam("result", loginResult.getQueryValue());

        if (loginResult == SocialLoginResult.SUCCESS) {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("로그인 사용자를 찾을 수 없습니다."));
            AuthService.LoginTokens loginTokens = authService.issueLoginTokens(user);
            targetUrlBuilder
                    .queryParam("accessToken", loginTokens.accessToken())
                    .queryParam("refreshToken", loginTokens.refreshToken())
                    .queryParam("nickname", user.getName());
        }

        clearAuthenticationAttributes(request);
        UriComponents targetUrl = targetUrlBuilder.build().encode(StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, targetUrl.toUriString());
    }

    private Long extractUserId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
