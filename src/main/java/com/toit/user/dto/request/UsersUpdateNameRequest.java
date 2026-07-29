package com.toit.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UsersUpdateNameRequest {

    @NotBlank(message = "닉네임은 필수 값입니다.")
    @Size(max = 10, message = "닉네임은 최대 10자까지 입력할 수 있습니다.")
    private String name;
}