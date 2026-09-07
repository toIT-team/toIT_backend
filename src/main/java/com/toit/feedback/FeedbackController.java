package com.toit.feedback;

import com.toit.common.SecurityUtil;
import com.toit.feedback.dto.request.FeedbackCreateRequest;
import com.toit.feedback.dto.response.FeedbackCreateResponse;
import com.toit.feedback.dto.response.FeedbackMyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Feedback", description = "피드백 및 문의 API")
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "피드백/문의 등록", description = "사용자가 피드백 또는 문의를 등록합니다.")
    @PostMapping
    public ResponseEntity<FeedbackCreateResponse> create(@RequestBody FeedbackCreateRequest request) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.create(usersId, request));
    }

    @Operation(summary = "내 피드백/문의 목록 조회", description = "로그인한 사용자의 피드백 및 문의 목록과 답변을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<List<FeedbackMyResponse>> getMyList() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(feedbackService.getMyList(usersId));
    }

    @Operation(summary = "내 피드백/문의 단건 조회", description = "알림함의 딥링크가 가리키는 문의 하나를 조회합니다.")
    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackMyResponse> getMyFeedback(@PathVariable("feedbackId") Long feedbackId) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(feedbackService.getMyFeedback(usersId, feedbackId));
    }

    @Operation(summary = "내 피드백/문의 삭제", description = "답변이 등록된 문의는 삭제할 수 없습니다.")
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> delete(@PathVariable("feedbackId") Long feedbackId) {
        Long usersId = SecurityUtil.getCurrentUserId();
        feedbackService.delete(usersId, feedbackId);
        return ResponseEntity.noContent().build();
    }
}