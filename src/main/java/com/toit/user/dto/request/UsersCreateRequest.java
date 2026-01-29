package com.toit.user.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersCreateRequest {

    /** 사용자 이메일 */
    private String email;

    /** 사용자 이름 */
    private String name;

    /** 사용자 자기소개 */
    private String bio;

    /**  "KAKAO", "GOOGLE" */
    private String authProvider;

    /** 사용자 OAuth 식별자 - OAuth 2.0으로 가져온 값 -> 랜던값으로 진행 */
    private Long providerUsersId;
}
