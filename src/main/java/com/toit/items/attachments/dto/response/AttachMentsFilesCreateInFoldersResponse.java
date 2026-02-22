package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsFilesCreateInFoldersResponse {
    private Long attachmentsId;
    private Long usersId;

    /**
     * ENUM형태("FILE")
     */
    private AttachMentsType attachmentsType;
    private String objectKey;
    private String presignedUrl;
    private Double attachmentsSize;
    private String fileName;
    private String attachmentsExtension;
    private LocalDateTime createdAt;

    public AttachMentsFilesCreateInFoldersResponse(Long attachmentsId, Long usersId, AttachMentsType attachmentsType,
                                                    String objectKey, String presignedUrl, String attachmentsExtension,
                                                    Double attachmentsSize,  String fileName, LocalDateTime createdAt){
        this.attachmentsId = attachmentsId;
        this.usersId = usersId;
        this.attachmentsType = attachmentsType;
        this.objectKey = objectKey;
        this.presignedUrl = presignedUrl;
        this.attachmentsExtension = attachmentsExtension;
        this.attachmentsSize = attachmentsSize;
        this.fileName = fileName;
        this.createdAt = createdAt;
    }

    public AttachMentsFilesCreateInFoldersResponse(AttachMents attachments) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.usersId = attachments.getUsers().getUsersId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = attachments.getPresignedUrl();
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        this.createdAt = attachments.getCreatedAt();
    }
}
