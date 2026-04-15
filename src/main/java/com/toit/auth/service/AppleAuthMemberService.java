package com.toit.auth.service;

import com.toit.auth.dto.AppleUserInfo;
import com.toit.auth.login.SocialLoginResult;
import com.toit.common.enums.AuthProvider;
import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import com.toit.usersinfo.UsersSettings;
import com.toit.usersinfo.UsersSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Apple OIDC 로그인 처리 서비스
 *
 * Apple은 userinfo endpoint 없이 ID Token(JWT) 안에 사용자 정보가 포함되는 OIDC 방식이므로
 * DefaultOAuth2UserService 대신 OidcUserService를 상속합니다.
 */
@Service
@RequiredArgsConstructor
public class AppleAuthMemberService extends OidcUserService {

    private final UsersRepository usersRepository;
    private final UsersSettingsRepository usersSettingsRepository;
    private final FoldersRepository foldersRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        AppleUserInfo userInfo = new AppleUserInfo(oidcUser);
        Users user = saveOrUpdate(userInfo);
        SocialLoginResult loginResult = resolveLoginResult(user);

        // AuthSuccessHandler가 읽는 toitUserId, toitLoginResult를 ID Token claims에 추가
        Map<String, Object> claims = new HashMap<>(oidcUser.getIdToken().getClaims());
        claims.put("toitUserId", user.getUsersId());
        claims.put("toitLoginResult", loginResult.name());

        OidcIdToken enrichedIdToken = new OidcIdToken(
                oidcUser.getIdToken().getTokenValue(),
                oidcUser.getIdToken().getIssuedAt(),
                oidcUser.getIdToken().getExpiresAt(),
                claims
        );

        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                enrichedIdToken,
                "sub"
        );
    }

    private Users saveOrUpdate(AppleUserInfo userInfo) {
        String providerId = userInfo.getProviderId();
        String fallbackName = "apple_" + providerId.substring(0, Math.min(8, providerId.length()));
        String resolvedName = hasText(userInfo.getName()) ? userInfo.getName() : fallbackName;

        return usersRepository.findByAuthProviderAndProviderUsersId(AuthProvider.APPLE, providerId)
                .map(entity -> {
                    // Apple은 최초 로그인 때만 이름을 전달하므로 실제 이름이 있을 때만 업데이트
                    entity.updateSocialProfile(userInfo.getEmail(), userInfo.getName(), null);
                    return entity;
                })
                .orElseGet(() -> {
                    Users savedUser = usersRepository.save(new Users(
                            userInfo.getEmail(),
                            resolvedName,
                            null,
                            null,
                            AuthProvider.APPLE,
                            providerId,
                            LocalDateTime.now()
                    ));
                    usersSettingsRepository.save(new UsersSettings(savedUser));
                    foldersRepository.save(new Folders("기본 보관함", null, true, "blue500", false, LocalDateTime.now(), savedUser, 0));
                    return savedUser;
                });
    }

    private SocialLoginResult resolveLoginResult(Users user) {
        if (user.isDeleted()) return SocialLoginResult.DELETED_USER;
        if (user.requiresAdditionalInfo()) return SocialLoginResult.NEEDS_ADDITIONAL_INFO;
        return SocialLoginResult.SUCCESS;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
