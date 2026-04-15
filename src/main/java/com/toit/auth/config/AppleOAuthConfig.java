package com.toit.auth.config;

import com.toit.auth.service.AppleClientSecretService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Apple OAuth2 토큰 요청 설정
 *
 * addParametersConverter는 기존 파라미터에 addAll로 합쳐지기 때문에
 * client_secret=placeholder와 jwt가 중복 전송됩니다.
 * setParametersConverter로 파라미터를 직접 빌드해야 Apple이 올바른 client_secret을 받습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppleOAuthConfig {

    private final AppleClientSecretService appleClientSecretService;

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        RestClientAuthorizationCodeTokenResponseClient client = new RestClientAuthorizationCodeTokenResponseClient();

        client.setParametersConverter(request -> {
            ClientRegistration reg = request.getClientRegistration();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.set("grant_type", "authorization_code");
            params.set("code", request.getAuthorizationExchange().getAuthorizationResponse().getCode());
            params.set("redirect_uri", request.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());
            params.set("client_id", reg.getClientId());

            // Apple: 동적 JWT / 그 외: properties의 client_secret 사용
            String clientSecret = "apple".equalsIgnoreCase(reg.getRegistrationId())
                    ? appleClientSecretService.generateClientSecret()
                    : reg.getClientSecret();
            params.set("client_secret", clientSecret);

            // ── 토큰 요청 파라미터 로그 ──
            log.info("[Apple Token Request] client_id     = {}", params.getFirst("client_id"));
            log.info("[Apple Token Request] redirect_uri  = {}", params.getFirst("redirect_uri"));
            log.info("[Apple Token Request] grant_type    = {}", params.getFirst("grant_type"));

            String secret = params.getFirst("client_secret");
            boolean isJwt = secret != null && secret.split("\\.").length == 3;
            log.info("[Apple Token Request] client_secret = {} (JWT형식: {})",
                    secret != null ? secret.substring(0, Math.min(30, secret.length())) + "..." : "null", isJwt);

            if (isJwt) {
                try {
                    // 서명 검증 없이 payload만 파싱
                    String payload = secret.split("\\.")[1];
                    byte[] decoded = java.util.Base64.getUrlDecoder().decode(
                            payload.length() % 4 == 0 ? payload : payload + "=".repeat(4 - payload.length() % 4));
                    String json = new String(decoded);
                    log.info("[Apple Token Request] JWT payload  = {}", json);
                } catch (Exception e) {
                    log.warn("[Apple Token Request] JWT payload 파싱 실패: {}", e.getMessage());
                }
            }

            return params;
        });

        return client;
    }
}