package com.toit.common.S3.attachmentprocessor;

import com.toit.common.S3.config.S3Config;
import com.toit.common.enums.AttachMentsType;
import com.toit.exception.items.attachments.AttachmentReadException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageAttachmentProcessor implements AttachmentProcessor {
    private final S3KeyFactory s3KeyFactory;
    private final S3Config s3Storage;

    @Override
    public AttachMentsType supports() {
        return AttachMentsType.IMAGE;
    }

    /**
     * 500 에러 == 서버 애러
     * @param image
     * @return
     */
    @Override
    public AttachmentPayload getSizeWithDimensions(MultipartFile image) {
        try {
            long bytesStart = System.currentTimeMillis();
            byte[] bytes = image.getBytes();
            long bytesReadMs = System.currentTimeMillis() - bytesStart;
            long size = bytes.length;

            Long width = null;
            Long height = null;

            // Flutter에서 imagesWidth/imagesHeight 미사용으로 ImageIO 디코딩 제거
            // long imageIoStart = System.currentTimeMillis();
            // BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            // long imageIoMs = System.currentTimeMillis() - imageIoStart;
            // if (img != null) {
            //     width = (long) img.getWidth();
            //     height = (long) img.getHeight();
            // }

            log.info("[이미지 수신 분석] file={} | bytesReadMs={}ms | size={}bytes",
                    image.getOriginalFilename(), bytesReadMs, size);

            String attachmentsExtension = image.getContentType();
            String fileName = image.getOriginalFilename();
            Double attachmentsSize = (double) size;
            /** png, pdf 이런 거 추출*/
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
            throw new AttachmentReadException("서버에서 이미지를 읽지 못 했거나 size와 크기를 읽지 못 했습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "bin";   // 기본값
        }
        String cleanName = filename.trim();
        int lastDotIndex = cleanName.lastIndexOf('.');
        // 점이 없거나, 맨 끝에 점이 있는 경우
        if (lastDotIndex == -1 || lastDotIndex == cleanName.length() - 1) {
            return "bin";
        }
        return cleanName.substring(lastDotIndex + 1).toLowerCase();
    }

    @Override
    public StorageResult S3Store(Long usersId, AttachmentPayload payload) {
        String objectKey = s3KeyFactory.imageKey(usersId, payload.getExt());

        long s3Start = System.currentTimeMillis();
        s3Storage.S3upload(
                objectKey,
                new ByteArrayInputStream(payload.getBytes()),
                payload.getAttachmentsExtension(),
                payload.getAttachmentsSize().longValue()
        );
        long s3UploadMs = System.currentTimeMillis() - s3Start;

        long presignStart = System.currentTimeMillis();
        String presignedUrl = s3Storage.presignGetUrl(objectKey, Duration.ofDays(7));
        long presignMs = System.currentTimeMillis() - presignStart;

        return new StorageResult(objectKey, presignedUrl, s3UploadMs, presignMs);
    }

}