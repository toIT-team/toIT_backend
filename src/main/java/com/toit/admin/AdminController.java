package com.toit.admin;

import com.toit.admin.dto.request.AdminLoginRequest;
import com.toit.admin.dto.request.AdminRegisterRequest;
import com.toit.admin.dto.response.AdminLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "관리자 로그인", description = "이메일과 비밀번호로 관리자 JWT를 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @Operation(summary = "관리자 등록", description = "시크릿 키 검증 후 관리자 계정을 생성합니다.")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AdminRegisterRequest request) {
        adminService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}