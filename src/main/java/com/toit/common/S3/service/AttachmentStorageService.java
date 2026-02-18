package com.toit.common.S3.service;

import com.toit.common.S3.S3KeyFactory;
import com.toit.common.S3.attachmentprocessor.AttachmentPayload;
import com.toit.common.S3.config.S3Config;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachmentStorageService {

    private final S3KeyFactory s3KeyFactory;
    private final S3Config s3Storage;

    public StorageResult storeImage(
            Long usersId,
            AttachmentPayload payload
    ) {

        /** 키 생성 확장자는 payload.getExtension() */
        String objectKey = s3KeyFactory.imageKey(
                usersId,
                payload.getExt()
        );

        /** S3 업로드 */
        s3Storage.S3upload(
                objectKey,
                new ByteArrayInputStream(payload.getBytes()),
                payload.getAttachmentsExtension(),
                payload.getAttachmentsSize().longValue()
        );

        /** presigned url 생성 (예: 30분) */
        String presignedUrl =
                s3Storage.presignGetUrl(objectKey, Duration.ofMinutes(30));

        return new StorageResult(objectKey, presignedUrl);
    }
}