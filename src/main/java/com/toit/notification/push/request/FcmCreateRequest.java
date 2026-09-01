package com.toit.notification.push.request;

import com.toit.notification.push.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmCreateRequest {

    /** FID. 앱에서 FirebaseInstallations.getId() 로 얻는다. */
    @NotBlank(message = "installationId는 필수 값입니다.")
    @Size(max = 64, message = "installationId는 64자를 넘을 수 없습니다.")
    private String installationId;

    @NotBlank(message = "fcmToken은 필수 값입니다.")
    private String fcmToken;

    @NotNull(message = "platform은 필수 값입니다. ANDROID 또는 IOS")
    private Platform platform;

    /** "14" · "17.2" 처럼 숫자만. 없어도 된다. */
    @Size(max = 20, message = "osVersion은 20자를 넘을 수 없습니다.")
    private String osVersion;
}
