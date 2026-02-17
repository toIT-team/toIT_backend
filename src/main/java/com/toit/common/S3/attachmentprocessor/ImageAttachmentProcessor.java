package com.toit.common.S3.attachmentprocessor;

import com.toit.common.enums.AttachMentsType;
import com.toit.exception.items.attachments.AttachmentReadException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageAttachmentProcessor implements AttachmentProcessor {

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
            /** bytes로 한 번만 읽어서 size + 및 이미지 크기 추출 */
            byte[] bytes = image.getBytes();
            long size = bytes.length;

            Long width = null;
            Long height = null;

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img != null) {
                width = (long) img.getWidth();
                height = (long) img.getHeight();
            }

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

}