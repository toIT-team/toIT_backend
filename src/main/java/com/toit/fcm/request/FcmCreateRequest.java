package com.toit.fcm.request;


import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmCreateRequest {


    // Long 타입은 빈 문자열("") 개념이 없으므로 @NotNull 사용
    @NotNull("usersId는 필수 값입니다.")
    private Long usersId;


    // String 타입은 null, "", " " 모두 막아야 하므로 @NotBlank 사용
    @NotBlank(message = "fcmToken은 필수 값입니다.")
    private String fcmToken;

}
