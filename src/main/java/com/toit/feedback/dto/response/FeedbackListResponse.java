package com.toit.feedback.dto.response;

import com.toit.feedback.Feedback;

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
        return new FeedbackListResponse(
                feedback.getFeedbackId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getUsers().getEmail(),
                feedback.getUsers().getName(),
                feedback.getCreatedAt(),
                feedback.getReply(),
                feedback.getRepliedAt(),
                adminName
        );
    }
}
