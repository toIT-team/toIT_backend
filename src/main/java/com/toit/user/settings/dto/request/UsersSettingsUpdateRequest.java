package com.toit.user.settings.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersSettingsUpdateRequest {

    //설정한 알림(on, off )
    private Boolean appAlarmEnabled ;

}
