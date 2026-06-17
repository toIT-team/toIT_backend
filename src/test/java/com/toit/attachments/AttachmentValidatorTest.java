package com.toit.attachments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.toit.common.S3.attachmentvaildator.AttachmentValidator;
import com.toit.exception.items.attachments.FileSizeExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AttachmentValidatorTest {

    private final AttachmentValidator attachmentValidator = new AttachmentValidator();

    private static final int MB = 1024 * 1024;

    @Test
    @DisplayName("20MB 이하 이미지 파일은 크기 검증을 통과한다")
    void validateFileSize_shouldPass_whenImageSizeIsUnderLimit() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                new byte[20 * MB]  // 정확히 20MB
        );

        assertDoesNotThrow(() -> attachmentValidator.validateFileSize(image.getSize()));
    }

    @Test
    @DisplayName("20MB를 초과한 이미지 파일은 예외를 던진다")
    void validateFileSize_shouldThrowException_whenImageSizeExceedsLimit() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "large.png",
                "image/png",
                new byte[20 * MB + 1]  // 20MB + 1byte
        );

        assertThrows(FileSizeExceededException.class,
                () -> attachmentValidator.validateFileSize(image.getSize()));
    }

    @Test
    @DisplayName("20MB를 초과한 파일(pdf 등)은 예외를 던진다")
    void validateFileSize_shouldThrowException_whenFileSizeExceedsLimit() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                new byte[20 * MB + 1]  // 20MB + 1byte
        );

        assertThrows(FileSizeExceededException.class,
                () -> attachmentValidator.validateFileSize(file.getSize()));
    }

    @Test
    @DisplayName("빈 파일은 크기 검증을 통과한다")
    void validateFileSize_shouldPass_whenFileSizeIsZero() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertDoesNotThrow(() -> attachmentValidator.validateFileSize(file.getSize()));
    }
}