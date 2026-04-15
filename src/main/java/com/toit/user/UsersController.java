package com.toit.user;

import com.toit.common.SecurityUtil;
import com.toit.user.dto.request.UsersCreateRequest;
import com.toit.user.dto.request.UsersUpdateNameRequest;
import com.toit.user.dto.response.UsersCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @Operation(
            summary = "이름 수정 API",
            description = "사용자의 이름을 수정합니다."
    )
    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(@RequestBody UsersUpdateNameRequest request) {
        Long usersId = SecurityUtil.getCurrentUserId();
        usersService.updateName(usersId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "회원 탈퇴 API",
            description = "회원 탈퇴 시 계정이 비활성화됩니다. (Soft Delete)"
    )
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw() {
        Long usersId = SecurityUtil.getCurrentUserId();
        usersService.withdraw(usersId);
        return ResponseEntity.noContent().build();
    }
}
