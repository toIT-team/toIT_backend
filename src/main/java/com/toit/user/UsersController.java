package com.toit.user;

import com.toit.user.dto.request.UsersCreateRequest;
import com.toit.user.dto.response.UsersCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    /** 사용자 생성 - 카카오 로그인 전 임시 */
    @Operation(
            summary = "사용자 생성 API - 화면이름 : X(임시 API)",
            description = "로그인이 없어 임시로 사용자를 생성해서 해야됩니다."
    )
    @PostMapping
    public ResponseEntity<UsersCreateResponse> createUser(
            @RequestBody UsersCreateRequest request
    ) {
        return ResponseEntity.ok(usersService.createUser(request));
    }
}
