package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AttachMentsConfirmResponse {
    private final Long attachmentsId;
    private final AttachMentsType attachmentsType;
    private final String objectKey;  // Flutter가 이 키로 CloudFront URL을 생성한다
    private final String fileName;
    private final Double attachmentsSize;
    private final String attachmentsExtension;
    // private final Long imagesWidth;
    // private final Long imagesHeight;
    private final String textContent;
    private final LocalDateTime createdAt;

    public AttachMentsConfirmResponse(AttachMents attachments) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.fileName = attachments.getFileName();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        // this.imagesWidth = attachments.getImagesWidth();
        // this.imagesHeight = attachments.getImagesHeight();
        this.textContent = attachments.getTextContent();
        this.createdAt = attachments.getCreatedAt();
    }
}