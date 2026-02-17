package com.toit.common.S3.attachmentvaildator;

import com.toit.exception.items.attachments.UnsupportedImageTypeException;
import org.springframework.stereotype.Component;

@Component
public class AttachmentValidator {

    /**
     * 확장자 검사 -> 400에러 반환
     */
    public void validateImageContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UnsupportedImageTypeException("이미지 파일만 업로드 가능합니다.");
        }
    }
}
