package com.toit.common.S3.attachmentprocessor;

import com.toit.common.S3.config.S3Config;
import com.toit.common.enums.AttachMentsType;
import com.toit.exception.items.attachments.AttachmentReadException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileAttachmentProcessor implements AttachmentProcessor {

    private final S3KeyFactory s3KeyFactory;
    private final S3Config s3Storage;

    @Override
    public AttachMentsType supports() {
        return AttachMentsType.FILE;
    }

    @Override
    public AttachmentPayload getSizeWithDimensions(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            long size = bytes.length;

            /** 이미지가 아니므로 width/height는 null */
            Long width = null;
            Long height = null;

            String attachmentsExtension = file.getContentType();
            String fileName = file.getOriginalFilename();
            Double attachmentsSize = (double) size;

            // pdf, zip, docx 등 확장자 추출
            String ext = getExtension(fileName);

            return new AttachmentPayload(
                    bytes,
                    attachmentsSize,
                    attachmentsExtension,
                    fileName,
                    ext,
                    width,
                    height
            );

        } catch (IOException e) {
            throw new AttachmentReadException("서버에서 파일을 읽지 못 했거나 size를 읽지 못 했습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "bin";
        }
        String cleanName = filename.trim();
        int lastDotIndex = cleanName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == cleanName.length() - 1) {
            return "bin";
        }
        return cleanName.substring(lastDotIndex + 1).toLowerCase();
    }

    @Override
    public StorageResult S3Store(Long usersId, AttachmentPayload payload){

        /** 키 생성 확장자는 payload.getExtension() */
        String objectKey = s3KeyFactory.fileKey(
                usersId,
                payload.getExt()
        );

        /** S3 업로드 */
        long s3Start = System.currentTimeMillis();
        s3Storage.S3upload(
                objectKey,
                new ByteArrayInputStream(payload.getBytes()),
                payload.getAttachmentsExtension(),
                payload.getAttachmentsSize().longValue()
        );
        long s3UploadMs = System.currentTimeMillis() - s3Start;

        /** SigV4 presigned URL은 최대 7일까지 허용 */
        long presignStart = System.currentTimeMillis();
        String presignedUrl = s3Storage.presignGetUrl(objectKey, Duration.ofDays(7));
        long presignMs = System.currentTimeMillis() - presignStart;

        return new StorageResult(objectKey, presignedUrl, s3UploadMs, presignMs);
    }

}
