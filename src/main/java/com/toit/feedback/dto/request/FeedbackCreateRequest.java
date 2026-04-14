package com.toit.feedback.dto.request;

public record FeedbackCreateRequest(
        String title,
        String content
) {}