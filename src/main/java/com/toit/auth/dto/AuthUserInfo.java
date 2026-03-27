package com.toit.auth.dto;

//OAuth2UserInfo.java (인터페이스)
public interface AuthUserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getName();
    String getImageUrl();
}
