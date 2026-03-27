package com.toit.auth.dto;

import java.util.Map;

public class KakaoUserInfo implements AuthUserInfo {
    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        return (account != null) ? (String) account.get("email") : null;
    }

    @Override
    public String getName() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if (account == null) return null;

        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        return (profile != null) ? (String) profile.get("nickname") : null;
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if (account == null) return null;

        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        return (profile != null) ? (String) profile.get("profile_image_url") : null;
    }
}