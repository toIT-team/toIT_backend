package com.toit.feedback.dto.response;

import com.toit.feedback.Feedback;

import java.time.LocalDateTime;

public record FeedbackListResponse(
        Long feedbackId,
        String title,
        String content,
        String userEmail,
        String userName,
        LocalDateTime createdAt
) {
    public static FeedbackListResponse from(Feedback feedback) {
        return new FeedbackListResponse(
                feedback.getFeedbackId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getUsers().getEmail(),
                feedback.getUsers().getName(),
                feedback.getCreatedAt()
        );
    }
}
