package com.toit.user.dto.response;

import com.toit.common.enums.AuthProvider;
import com.toit.user.Users;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersCreateResponse {

    private Long usersId;
    private String email;
    private String name;
    private AuthProvider authProvider;
    private Long providerUsersId;
    private LocalDateTime createdAt;

    public UsersCreateResponse(Users users) {
        this.usersId = users.getUsersId();
        this.email = users.getEmail();
        this.name = users.getName();
        this.authProvider = users.getAuthProvider();
        this.providerUsersId = users.getProviderUsersId();
        this.createdAt = users.getCreatedAt();
    }
}