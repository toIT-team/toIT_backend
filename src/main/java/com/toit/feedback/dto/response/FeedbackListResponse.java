package com.toit.feedback.dto.response;

import com.toit.feedback.Feedback;
import com.toit.user.Users;

import java.time.LocalDateTime;

public record FeedbackListResponse(
        Long feedbackId,
        String title,
        String content,
        String userEmail,
        String userName,
        LocalDateTime createdAt,
        String reply,
        LocalDateTime repliedAt,
        String adminName
) {
    public static FeedbackListResponse from(Feedback feedback, String adminName) {
        // 탈퇴 회원의 피드백은 작성자가 익명화(null)되어 남는다.
        Users writer = feedback.getUsers();
        return new FeedbackListResponse(
                feedback.getFeedbackId(),
                feedback.getTitle(),
                feedback.getContent(),
                writer == null ? null : writer.getEmail(),
                writer == null ? "탈퇴한 사용자" : writer.getName(),
                feedback.getCreatedAt(),
                feedback.getReply(),
                feedback.getRepliedAt(),
                adminName
        );
    }
}
