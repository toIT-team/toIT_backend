package com.toit.auth.service;

import com.toit.auth.dto.AuthUserInfo;
import com.toit.auth.dto.KakaoUserInfo;
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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthMemberService extends DefaultOAuth2UserService {

    private final UsersRepository usersRepository;
    private final UsersSettingsRepository usersSettingsRepository;
    private final FoldersRepository foldersRepository;

    /**
     * [핵심 메서드] 소셜 서비스(카카오 등)로부터 유저 정보를 가져와서 우리 시스템의 유저로 변환합니다.
     * 인가 코드를 통해 엑세스 토큰을 받은 직후, 스프링 시큐리티에 의해 자동으로 호출됩니다.
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 카카오 API 서버에 토큰을 던져서 "원본 유저 데이터(Map 형태)"를 받아옵니다.
        // 이 메서드가 실행될 때 실제로 외부 서버와 통신이 일어납니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 현재 로그인을 시도한 서비스가 어디인지 식별 값을 가져옵니다. (예: "kakao")
        // 나중에 애플 로그인이 추가되면 이 값으로 구분하게 됩니다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 공급자별로 제각각인 데이터 구조를 우리 서버 전용 인터페이스(AuthUserInfo)로 규격화합니다.
        // 전략 패턴을 사용하여 카카오 전용 번역기(KakaoUserInfo)를 생성합니다.
        AuthUserInfo userInfo = null;
        if (registrationId.equalsIgnoreCase("kakao")) {
            userInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        }

        // 지원하지 않는 서비스(예: 구글 버튼을 억지로 눌렀을 때 등)일 경우 에러를 던져 중단합니다.
        if (userInfo == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_social_provider"),
                    "지원하지 않는 소셜 로그인입니다."
            );
        }

        // 4. 번역된 정보를 우리 DB(PostgreSQL)에 동기화합니다.
        // 이 과정에서 회원가입이 되거나, 기존 회원의 정보가 최신화됩니다.
        Users user = saveOrUpdate(userInfo);
        SocialLoginResult loginResult = resolveLoginResult(user);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("toitUserId", user.getUsersId());
        attributes.put("toitLoginResult", loginResult.name());

        // 5. 스프링 시큐리티 세션에 "로그인 성공 유저"임을 증명하는 객체를 반환합니다.
        /**
         *  DefaultOAuth2User 멤버변수
         *  authorities -> ROLE_USER 등 권한
         *  attributes -> 카카오 원본 데이터 + toitUserId + toitLoginResult
         *  nameAttributeKey -> attributes 중 식별자 로 쓸 키 (id)
         */
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userRequest.getClientRegistration().getProviderDetails()
                        .getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    /**
     * 유저 정보를 DB에 저장하거나 이미 있다면 업데이트합니다.
     */
    private Users saveOrUpdate(AuthUserInfo userInfo) {
        AuthProvider authProvider = AuthProvider.valueOf(userInfo.getProvider().toUpperCase());
        String providerUsersId = userInfo.getProviderId();
        String fallbackName = userInfo.getProvider() + "_" + providerUsersId;
        String resolvedName = hasText(userInfo.getName()) ? userInfo.getName() : fallbackName;

        return usersRepository.findByAuthProviderAndProviderUsersId(authProvider, providerUsersId)
                .map(entity -> {
                    // 이미 가입된 계정은 이름을 덮어씌우지 않음
                    entity.updateSocialProfile(userInfo.getEmail(), null, userInfo.getImageUrl());
                    return entity;
                })
                .orElseGet(() -> {
                    Users savedUser = usersRepository.save(new Users(
                            userInfo.getEmail(),
                            resolvedName,
                            null, // bio는 null로 보내도 엔티티에서 기본값 처리 가능
                            userInfo.getImageUrl(),
                            authProvider,
                            providerUsersId,
                            LocalDateTime.now()
                    ));
                    usersSettingsRepository.save(new UsersSettings(savedUser));
                    foldersRepository.save(new Folders("기본 보관함", null, true, "blue500", false, LocalDateTime.now(), savedUser, 0));
                    return savedUser;
                });
    }

    private SocialLoginResult resolveLoginResult(Users user) {
        if (user.isDeleted()) {
            return SocialLoginResult.DELETED_USER;
        }
        if (user.requiresAdditionalInfo()) {
            return SocialLoginResult.NEEDS_ADDITIONAL_INFO;
        }
        return SocialLoginResult.SUCCESS;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
