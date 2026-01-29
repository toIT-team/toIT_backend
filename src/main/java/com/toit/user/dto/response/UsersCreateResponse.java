package com.toit.user.dto.response;

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
    private LocalDateTime createdAt;

    public UsersCreateResponse(Users users) {
        this.usersId = users.getUsersId();
        this.email = users.getEmail();
        this.name = users.getName();
        this.createdAt = users.getCreatedAt();
    }
}