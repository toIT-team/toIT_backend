package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsUpdateFileNameResponse {
    private Long attachmentsId;
    private Long foldersId;
    private AttachMentsType attachmentsType;
    private String fileName;
    private String objectKey;
    private String presignedUrl;
    private String attachmentsExtension;
    private Double attachmentsSize;
    private String textContent;
    private LocalDateTime createdAt;

    public AttachMentsUpdateFileNameResponse(AttachMents attachments, String signedUrl) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.foldersId = attachments.getStorageId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.fileName = attachments.getFileName();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = signedUrl;
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.textContent = attachments.getTextContent();
        this.createdAt = attachments.getCreatedAt();
    }
}