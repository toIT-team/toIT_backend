package com.toit.auth.login;

public enum SocialLoginResult {

    SUCCESS("success"),
    CANCELLED("cancelled"),
    NEEDS_SIGNUP("needs_signup"),
    NEEDS_ADDITIONAL_INFO("needs_additional_info"),
    BLOCKED("blocked"),
    FAILED("failed");

    private final String queryValue;

    SocialLoginResult(String queryValue) {
        this.queryValue = queryValue;
    }

    public String getQueryValue() {
        return queryValue;
    }

    public static SocialLoginResult fromName(String value) {
        return SocialLoginResult.valueOf(value);
    }
}
