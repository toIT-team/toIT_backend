package com.toit.admin.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRegisterRequest {

    private String email;
    private String password;
    private String name;
    private String secretKey;
}
