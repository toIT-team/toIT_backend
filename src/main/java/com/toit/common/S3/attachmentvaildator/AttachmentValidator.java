package com.toit.common.S3.attachmentvaildator;

import com.toit.exception.items.attachments.FileSizeExceededException;
import com.toit.exception.items.attachments.UnsupportedFileTypeException;
import com.toit.exception.items.attachments.UnsupportedImageTypeException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AttachmentValidator {

    private static final Logger log = LoggerFactory.getLogger(AttachmentValidator.class);

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB

    private static final List<String> ALLOWED_TYPES_FILES = List.of(
            "application/pdf",
            "application/msword",                                                                        // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",                  // .docx
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",                        // .xlsx
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",                // .pptx
            "text/plain",                                                                                // .txt
            "application/x-hwp",                                                                         // .hwp
            "application/haansofthwp",                                                                   // .hwp (일부 OS)
            "application/vnd.hancom.hwp",                                                                // .hwp (공식 MIME)
            "application/hwp",                                                                           // .hwp (일부 환경)
            "application/octet-stream",                                                                  // 확장자 기반으로 추가 검증
            "application/zip",                                                                           // .zip
            "application/x-zip-compressed",                                                              // .zip (일부 환경)
            "application/vnd.hancom.hwpx",                                                               // .hwpx (공식 MIME)
            "application/x-hwpx",                                                                        // .hwpx (일부 환경)
            "application/vnd.ms-powerpoint"                                                              // .ppt
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xlsx", "pptx", "ppt", "txt", "hwp", "hwpx", "zip"
    );

    /**
     * 확장자 검사 -> 400에러 반환
     */
    public void validateImageContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UnsupportedImageTypeException("이미지 파일만 업로드 가능합니다.");
        }
    }

    public void validateFilesContentType(String contentType, String originalFilename) {
        log.info("파일 업로드 contentType={}, filename={}", contentType, originalFilename);

        if (contentType == null) {
            throw new UnsupportedFileTypeException("허용되지 않은 파일 형식입니다.");
        }

        if (ALLOWED_TYPES_FILES.contains(contentType)) {
            // octet-stream인 경우 확장자로 추가 검증
            if ("application/octet-stream".equals(contentType)) {
                String ext = extractExtension(originalFilename);
                if (!ALLOWED_EXTENSIONS.contains(ext)) {
                    throw new UnsupportedFileTypeException("허용되지 않은 파일 형식입니다. (확장자: " + ext + ")");
                }
            }
            return;
        }

        throw new UnsupportedFileTypeException("허용되지 않은 파일 형식입니다.");
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) return "";
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1).toLowerCase();
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
