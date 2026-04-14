package com.toit.feedback;

import com.toit.common.SecurityUtil;
import com.toit.feedback.dto.request.FeedbackCreateRequest;
import com.toit.feedback.dto.response.FeedbackCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}