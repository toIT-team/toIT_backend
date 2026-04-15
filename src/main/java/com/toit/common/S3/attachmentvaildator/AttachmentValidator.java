package com.toit.common.S3.attachmentvaildator;

import com.toit.exception.items.attachments.FileSizeExceededException;
import com.toit.exception.items.attachments.UnsupportedFileTypeException;
import com.toit.exception.items.attachments.UnsupportedImageTypeException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AttachmentValidator {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB

    private static final List<String> ALLOWED_TYPES_FILES = List.of(
            "application/pdf",
            "application/msword",                                                                        // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",                  // .docx
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",                        // .xlsx
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",                // .pptx
            "text/plain",                                                                                // .txt
            "application/x-hwp",                                                                         // .hwp
            "application/haansofthwp"                                                                    // .hwp (일부 OS 다른 MIME)
    );

    /**
     * 확장자 검사 -> 400에러 반환
     */
    public void validateImageContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UnsupportedImageTypeException("이미지 파일만 업로드 가능합니다.");
        }
    }

    public void validateFilesContentType(String contentType) {
        if (contentType == null || !ALLOWED_TYPES_FILES.contains(contentType)) {
            throw new UnsupportedFileTypeException("허용되지 않은 파일 형식입니다.");
        }
    }

    /**
     * 파일 크기 검사 -> 10MB 초과 시 400에러 반환
     */
    public void validateFileSize(long fileSize) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }
}
