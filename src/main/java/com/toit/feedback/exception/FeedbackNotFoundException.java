package com.toit.feedback.exception;

// 피드백·문의에 관한 404 에러
public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(String message) {
        super(message);
    }
}
