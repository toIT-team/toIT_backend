package com.toit.admin;

import com.toit.admin.dto.request.AdminLoginRequest;
import com.toit.admin.dto.request.AdminRegisterRequest;
import com.toit.admin.dto.response.AdminLoginResponse;
import com.toit.auth.jwt.JwtProvider;
import com.toit.common.UsersRole.UsersRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${toit.admin.secret-key}")
    private String adminSecretKey;

    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(
                admin.getAdminId(),
                admin.getEmail(),
                null,
                UsersRole.ROLE_ADMIN
        );

        return new AdminLoginResponse(accessToken);
    }

    public void register(AdminRegisterRequest request) {
        if (!adminSecretKey.equals(request.getSecretKey())) {
            throw new IllegalArgumentException("유효하지 않은 시크릿 키입니다.");
        }
        if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 관리자입니다.");
        }
        adminRepository.save(new Admin(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName()
        ));
    }
}