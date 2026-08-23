package com.toit.notification.push.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmCreateRequest {

    @NotBlank(message = "fcmToken은 필수 값입니다.")
    private String fcmToken;
}