package com.toit.view.pagemy.dto.response;

import com.toit.user.Users;
import lombok.Getter;

@Getter
public class PageMyViewResponse {

    private final String nickname;
    private final String imageUrl;
    private final String name;
    private final String email;
    private final String authProvider;

    public PageMyViewResponse(Users user) {
        this.nickname = user.getName();
        this.imageUrl = user.getImageUrl();
        this.name = user.getName();
        this.email = user.getEmail();
        this.authProvider = user.getAuthProvider().name();
    }
}
