package com.toit.admin;

import com.toit.admin.dto.request.AdminLoginRequest;
import com.toit.admin.dto.request.AdminRegisterRequest;
import com.toit.admin.dto.response.AdminLoginResponse;
import com.toit.common.SecurityUtil;
import com.toit.feedback.FeedbackService;
import com.toit.feedback.dto.request.FeedbackReplyRequest;
import com.toit.feedback.dto.response.FeedbackListResponse;
import com.toit.noitce.NoticeService;
import com.toit.noitce.dto.request.NoticeCreateRequest;
import com.toit.noitce.dto.request.NoticeDeleteRequest;
import com.toit.noitce.dto.request.NoticeUpdateRequest;
import com.toit.noitce.dto.response.NoticeDeleteResponse;
import com.toit.noitce.dto.response.NoticeUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FeedbackService feedbackService;
    private final NoticeService noticeService;

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
        feedbackService.reply(feedbackId, request);
        return ResponseEntity.noContent().build();
    }

    /***
     * 공지사항 관련
     */

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
    @PutMapping("/notices")
    public NoticeUpdateResponse updateNotice(
            @Valid @RequestBody NoticeUpdateRequest request
    ){
        Long adminId = SecurityUtil.getCurrentUserId();
        return noticeService.updateNotice(adminId, request);
    }

    @Operation(summary = "관리자 공지사항 삭제", description = "관리자가 공지사항을 삭제 합니다.")
    @DeleteMapping("/notices")
    public NoticeDeleteResponse deleteNotice(
            @Valid @RequestBody NoticeDeleteRequest request
    ){
        Long adminId = SecurityUtil.getCurrentUserId();
        return noticeService.deleteNotice(adminId, request);
    }

}
