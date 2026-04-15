package com.toit.admin.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AdminLoginResponse {

    private final String accessToken;
}