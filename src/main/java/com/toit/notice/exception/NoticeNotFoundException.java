package com.toit.notice.exception;

// 공지사항에 관한 404 에러
public class NoticeNotFoundException extends RuntimeException {
    public NoticeNotFoundException(String message) {
        super(message);
    }
}
