package com.toit.admin;

import com.toit.admin.dto.request.AdminLoginRequest;
import com.toit.admin.dto.request.AdminMemberCreateRequest;
import com.toit.admin.dto.request.AdminRegisterRequest;
import com.toit.admin.dto.response.AdminMemberResponse;
import com.toit.auth.jwt.JwtProvider;
import com.toit.common.UsersRole.UsersRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${toit.admin.secret-key}")
    private String adminSecretKey;

    public AdminLoginResult login(AdminLoginRequest request) {
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

        return new AdminLoginResult(accessToken, AdminMemberResponse.from(admin));
    }

    /**
     * 현재 로그인한 관리자 정보 조회 (쿠키 인증 확인 및 본인 식별용)
     */
    @Transactional(readOnly = true)
    public AdminMemberResponse getMe(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
        return AdminMemberResponse.from(admin);
    }

    /**
     * 로그인 결과: 쿠키에 담을 토큰과 응답 body로 내려줄 프로필
     */
    public record AdminLoginResult(String accessToken, AdminMemberResponse profile) {
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

    /**
     * 관리자 목록 조회 (관리자 페이지 전용)
     */
    @Transactional(readOnly = true)
    public List<AdminMemberResponse> getMembers() {
        return adminRepository.findAll().stream()
                .map(AdminMemberResponse::from)
                .toList();
    }

    /**
     * 관리자 추가 (로그인한 관리자만 호출 가능)
     */
    @Transactional
    public AdminMemberResponse createMember(AdminMemberCreateRequest request) {
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 관리자입니다.");
        }
        Admin admin = adminRepository.save(new Admin(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName()
        ));
        return AdminMemberResponse.from(admin);
    }

    /**
     * 관리자 삭제 (로그인한 관리자만 호출 가능)
     * 자기 자신은 삭제할 수 없고, 마지막 관리자도 삭제할 수 없습니다.
     */
    @Transactional
    public void deleteMember(Long targetAdminId, Long currentAdminId) {
        if (targetAdminId.equals(currentAdminId)) {
            throw new IllegalArgumentException("자기 자신은 삭제할 수 없습니다.");
        }
        if (!adminRepository.existsById(targetAdminId)) {
            throw new IllegalArgumentException("존재하지 않는 관리자입니다.");
        }
        if (adminRepository.count() <= 1) {
            throw new IllegalArgumentException("마지막 관리자는 삭제할 수 없습니다.");
        }
        adminRepository.deleteById(targetAdminId);
    }
}