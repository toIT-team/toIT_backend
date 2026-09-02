package com.toit.admin;

import com.toit.admin.dto.request.AdminLoginRequest;
import com.toit.admin.dto.request.AdminMemberCreateRequest;
import com.toit.admin.dto.request.AdminRegisterRequest;
import com.toit.admin.dto.response.AdminMemberResponse;
import com.toit.common.SecurityUtil;
import com.toit.feedback.FeedbackService;
import com.toit.feedback.dto.request.FeedbackReplyRequest;
import com.toit.feedback.dto.response.FeedbackListResponse;
import com.toit.notice.NoticeService;
import com.toit.items.attachments.storage.StorageService;
import com.toit.items.attachments.storage.dto.AdminStorageUsageResponse;
import java.util.List;
import com.toit.notice.dto.request.NoticeCreateRequest;
import com.toit.notice.dto.request.NoticeUpdateRequest;
import com.toit.notice.dto.response.NoticeDeleteResponse;
import com.toit.notice.dto.response.NoticeReadResponse;
import com.toit.notice.dto.response.NoticeUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String ADMIN_TOKEN_COOKIE = "admin_token";

    private final AdminService adminService;
    private final FeedbackService feedbackService;
    private final NoticeService noticeService;
    private final StorageService storageService;

    @Value("${ACCESS_TOKEN_EXPIRATION}")
    private long accessTokenExpiration;

    @Operation(summary = "관리자 로그인", description = "이메일/비밀번호를 검증하고 JWT를 httpOnly 쿠키로 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<AdminMemberResponse> login(@RequestBody AdminLoginRequest request) {
        AdminService.AdminLoginResult result = adminService.login(request);
        ResponseCookie cookie = buildTokenCookie(result.accessToken(), Duration.ofMillis(accessTokenExpiration));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.profile());
    }

    @Operation(summary = "관리자 로그아웃", description = "인증 쿠키를 만료시켜 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = buildTokenCookie("", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @Operation(summary = "현재 관리자 정보", description = "쿠키로 인증된 현재 관리자 정보를 반환합니다. (인증 확인용)")
    @GetMapping("/me")
    public ResponseEntity<AdminMemberResponse> me() {
        return ResponseEntity.ok(adminService.getMe(SecurityUtil.getCurrentUserId()));
    }

    @Operation(summary = "관리자 등록", description = "시크릿 키 검증 후 관리자 계정을 생성합니다.")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AdminRegisterRequest request) {
        adminService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 관리자 인증 토큰을 담는 httpOnly 쿠키를 생성합니다.
     * - httpOnly: JS 접근 차단(XSS로 토큰 탈취 방지)
     * - secure: HTTPS에서만 전송
     * - sameSite=Lax: admin.toit.cloud ↔ api.toit.cloud 는 같은 site(toit.cloud)라 정상 전송되며 CSRF를 방어
     */
    private ResponseCookie buildTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(ADMIN_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    @Operation(summary = "관리자 목록 조회", description = "등록된 관리자 목록을 조회합니다. (관리자 인증 필요)")
    @GetMapping("/members")
    public ResponseEntity<List<AdminMemberResponse>> getMembers() {
        return ResponseEntity.ok(adminService.getMembers());
    }

    @Operation(summary = "관리자 추가", description = "로그인한 관리자가 새로운 관리자 계정을 생성합니다.")
    @PostMapping("/members")
    public ResponseEntity<AdminMemberResponse> createMember(@RequestBody AdminMemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createMember(request));
    }

    @Operation(summary = "관리자 삭제", description = "로그인한 관리자가 다른 관리자 계정을 삭제합니다.")
    @DeleteMapping("/members/{adminId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long adminId) {
        Long currentAdminId = SecurityUtil.getCurrentUserId();
        adminService.deleteMember(adminId, currentAdminId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "피드백/문의 목록 조회", description = "사용자가 등록한 피드백 및 문의 목록을 조회합니다.")
    @GetMapping("/feedbacks")
    public ResponseEntity<Page<FeedbackListResponse>> getFeedbacks(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(feedbackService.getList(pageable));
    }

    @Operation(summary = "피드백/문의 답변 등록", description = "관리자가 피드백 또는 문의에 답변을 등록합니다.")
    @PostMapping("/feedbacks/{feedbackId}/reply")
    public ResponseEntity<Void> reply(
            @PathVariable Long feedbackId,
            @RequestBody FeedbackReplyRequest request
    ) {
        Long adminId = SecurityUtil.getCurrentUserId();
        feedbackService.reply(feedbackId, adminId, request);
        return ResponseEntity.noContent().build();
    }

    /***
     * 공지사항 관련
     */

    @Operation(summary = "관리자 공지사항 목록 조회", description = "관리자가 공지사항 목록을 조회합니다.")
    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeReadResponse>> getNotices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(noticeService.getNotices(pageable));
    }

    @Operation(summary = "전체 사용자 스토리지 사용량 조회", description = "사용자별 S3 스토리지 사용량을 조회합니다.")
    @GetMapping("/storage")
    public ResponseEntity<List<AdminStorageUsageResponse>> getAllStorage() {
        return ResponseEntity.ok(storageService.getAllUsage());
    }

    @Operation(summary = "관리자 공지사항 작성", description = "관리자가 공지사항을 생성합니다.")
    @PostMapping("/notices")
    public ResponseEntity<Void> createNotice(
            @Valid @RequestBody NoticeCreateRequest request
    ){
        Long adminId = SecurityUtil.getCurrentUserId();
        noticeService.createNotice(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "관리자 공지사항 수정", description = "관리자가 공지사항을 합니다.")
    @PutMapping("/notices/{noticeId}")
    public NoticeUpdateResponse updateNotice(
            @PathVariable("noticeId") Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request
    ){
        Long adminId = SecurityUtil.getCurrentUserId();
        return noticeService.updateNotice(adminId, noticeId, request);
    }

    @Operation(summary = "관리자 공지사항 삭제", description = "관리자가 공지사항을 삭제 합니다.")
    @DeleteMapping("/notices/{noticeId}")
    public NoticeDeleteResponse deleteNotice(
            @PathVariable("noticeId") Long noticeId
    ){
        Long adminId = SecurityUtil.getCurrentUserId();
        return noticeService.deleteNotice(adminId, noticeId);
    }



}
