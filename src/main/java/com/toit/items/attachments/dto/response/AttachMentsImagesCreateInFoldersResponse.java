package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsImagesCreateInFoldersResponse {
    private Long attachmentsId;
    private AttachMentsType attachmentsType;
    private String objectKey;
    private String presignedUrl;
    private Double attachmentsSize;
    private String fileName;
    // private Long imagesWidth;
    // private Long imagesHeight;
    private String attachmentsExtension;
    private String textContent;
    private LocalDateTime createdAt;

    public AttachMentsImagesCreateInFoldersResponse(AttachMents attachments, String signedUrl) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = signedUrl;
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        // this.imagesWidth = attachments.getImagesWidth();
        // this.imagesHeight = attachments.getImagesHeight();
        this.textContent = attachments.getTextContent();
        this.createdAt = attachments.getCreatedAt();
    }
}