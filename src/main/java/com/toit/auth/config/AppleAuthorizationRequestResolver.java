package com.toit.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

/**
 * Apple OAuth2 Authorization Request 커스터마이저
 *
 * Apple은 name 또는 email scope 요청 시 response_mode=form_post 필수.
 * Spring Security 기본 구현은 이 파라미터를 추가하지 않으므로 직접 주입합니다.
 */
public class AppleAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public AppleAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return applyAppleCustomization(defaultResolver.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return applyAppleCustomization(defaultResolver.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest applyAppleCustomization(OAuth2AuthorizationRequest request) {
        if (request == null) {
            return null;
        }
        String registrationId = (String) request.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID);
        if (!"apple".equals(registrationId)) {
            return request;
        }
        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(params -> params.put("response_mode", "form_post"))
                .build();
    }
}
