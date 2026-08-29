package com.toit.notification.push.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmCreateResponse {

    private Long usersId;
    private String fcmToken;

    public FcmCreateResponse(Long usersId, String fcmToken) {
        this.usersId = usersId;
        this.fcmToken = fcmToken;
    }
}
