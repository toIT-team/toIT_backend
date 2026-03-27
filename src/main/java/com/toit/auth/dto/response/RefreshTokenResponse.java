package com.toit.auth.dto.response;



import lombok.Getter;

@Getter

public class RefreshTokenResponse {
    private String accessToken;

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
