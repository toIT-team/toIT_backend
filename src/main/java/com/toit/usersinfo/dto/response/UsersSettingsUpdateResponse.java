package com.toit.usersinfo.dto.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersSettingsUpdateResponse {
    public UsersSettingsUpdateResponse(Boolean appAlarmEnabled) {
        this.appAlarmEnabled = appAlarmEnabled;
    }

    //설정한 알림(on, off )
    private Boolean appAlarmEnabled ;

}
