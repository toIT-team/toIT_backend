package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsImagesCreateInFoldersResponse{
    private Long attachmentsId;

    /**
     * ENUM형태("IMAGE")
     */
    private AttachMentsType attachmentsType;
    private String objectKey;
    private String presignedUrl;
    private Double attachmentsSize;
    private String fileName;
    private Long imagesWidth;
    private Long imagesHeight;
    private String attachmentsExtension;
    private String textContent;
    private LocalDateTime createdAt;

    public AttachMentsImagesCreateInFoldersResponse(Long attachmentsId, AttachMentsType attachmentsType,
                                                 String objectKey, String presignedUrl, String attachmentsExtension,
                                                 Double attachmentsSize,  String fileName, Long imagesWidth,
                                                    Long imagesHeight, LocalDateTime createdAt){
        this.attachmentsId = attachmentsId;
        this.attachmentsType = attachmentsType;
        this.objectKey = objectKey;
        this.presignedUrl = presignedUrl;
        this.attachmentsExtension = attachmentsExtension;
        this.attachmentsSize = attachmentsSize;
        this.fileName = fileName;
        this.imagesWidth = imagesWidth;
        this.imagesHeight = imagesHeight;
        this.createdAt = createdAt;
    }

    public AttachMentsImagesCreateInFoldersResponse(AttachMents attachments) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = attachments.getPresignedUrl();
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        this.imagesWidth = attachments.getImagesWidth();
        this.imagesHeight = attachments.getImagesHeight();
        this.textContent = attachments.getTextContent();
        this.createdAt = attachments.getCreatedAt();
    }
}