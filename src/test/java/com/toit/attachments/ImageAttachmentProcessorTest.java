package com.toit.attachments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toit.common.S3.attachmentprocessor.AttachmentPayload;
import com.toit.common.S3.attachmentprocessor.ImageAttachmentProcessor;
import com.toit.common.S3.attachmentprocessor.S3KeyFactory;
import com.toit.common.S3.attachmentprocessor.StorageResult;
import com.toit.common.S3.config.S3Config;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageAttachmentProcessorTest {

    @InjectMocks
    private ImageAttachmentProcessor imageAttachmentProcessor;

    @Mock
    private S3KeyFactory s3KeyFactory;

    @Mock
    private S3Config s3Config;

    @Test
    @DisplayName("S3Store는 S3에 업로드하고 presigned GET URL을 발급한다")
    void S3Store_shouldUploadToS3AndGeneratePresignedUrl() {
        Long usersId = 1L;
        AttachmentPayload payload = new AttachmentPayload(
                new byte[]{1, 2, 3},
                3.0,
                "image/png",
                "test.png",
                "png",
                100L,
                100L
        );

        when(s3KeyFactory.imageKey(anyLong(), anyString())).thenReturn("users/1/images/test.png");
        when(s3Config.presignGetUrl(anyString(), any(Duration.class))).thenReturn("https://presigned-url");

        StorageResult result = imageAttachmentProcessor.S3Store(usersId, payload);

        verify(s3Config).S3upload(anyString(), any(), anyString(), anyLong());
        verify(s3Config).presignGetUrl(anyString(), any(Duration.class));
        assertEquals("users/1/images/test.png", result.getObjectKey());
        assertEquals("https://presigned-url", result.getPresignedUrl());
        assertEquals(0L, result.getS3UploadMs(), 500L);
        assertEquals(0L, result.getPresignMs(), 500L);
    }
}