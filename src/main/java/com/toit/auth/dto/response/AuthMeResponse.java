package com.toit.auth.dto.response;

import com.toit.common.enums.AuthProvider;
import com.toit.user.Users;

/**
 * 로그인 상태/온보딩 확인 응답.
 * needsNickname 이 true 이면 아직 닉네임을 지정하지 않은 신규 사용자이므로,
 * 클라이언트는 닉네임 입력 화면으로 분기한 뒤 닉네임 지정 API를 호출해야 한다.
 */
public record AuthMeResponse(
        Long userId,
        String email,
        String nickname,
        AuthProvider provider,
        boolean needsNickname
) {
    public static AuthMeResponse from(Users user) {
        return new AuthMeResponse(
                user.getUsersId(),
                user.getEmail(),
                user.getName(),
                user.getAuthProvider(),
                !user.isNicknameSet()
        );
    }
}
