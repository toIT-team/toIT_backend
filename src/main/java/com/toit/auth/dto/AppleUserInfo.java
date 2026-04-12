package com.toit.auth.dto;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Apple OIDC ID Token에서 사용자 정보를 추출하는 DTO
 *
 * Apple은 userinfo endpoint가 없고 ID Token(JWT) 안에 사용자 정보가 포함됩니다.
 * name은 최초 로그인 시에만 제공되며, 이후 로그인에서는 null일 수 있습니다.
 */
public class AppleUserInfo {

    private final OidcUser oidcUser;

    public AppleUserInfo(OidcUser oidcUser) {
        this.oidcUser = oidcUser;
    }

    public String getProviderId() {
        return oidcUser.getSubject();
    }

    public String getEmail() {
        return oidcUser.getEmail();
    }

    // Apple은 최초 로그인 시에만 name을 제공합니다.
    public String getName() {
        return oidcUser.getFullName();
    }
}
