package com.toit.feedback.dto.response;

import com.toit.feedback.Feedback;

import java.time.LocalDateTime;

public record FeedbackMyResponse(
        Long feedbackId,
        String title,
        String content,
        LocalDateTime createdAt,
        String reply,
        LocalDateTime repliedAt
) {
    public static FeedbackMyResponse from(Feedback feedback) {
        return new FeedbackMyResponse(
                feedback.getFeedbackId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getCreatedAt(),
                feedback.getReply(),
                feedback.getRepliedAt()
        );
    }
}