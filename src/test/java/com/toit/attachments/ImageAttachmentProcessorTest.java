package com.toit.attachments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toit.common.S3.attachmentprocessor.AttachmentPayload;
import com.toit.common.S3.attachmentprocessor.ImageAttachmentProcessor;
import com.toit.common.S3.attachmentprocessor.S3KeyFactory;
import com.toit.common.S3.config.S3Config;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    @DisplayName("S3Store는 presigned URL을 7일 기간으로 요청한다")
    void S3Store_shouldRequestPresignedUrlWithSevenDays() {
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

        imageAttachmentProcessor.S3Store(usersId, payload);

        verify(s3Config).presignGetUrl(anyString(), eq(Duration.ofDays(7)));
    }

}
