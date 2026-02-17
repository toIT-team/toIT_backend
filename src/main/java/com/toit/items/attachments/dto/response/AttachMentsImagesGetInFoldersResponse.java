package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsImagesGetInFoldersResponse {
    private Long attachmentsId;

    private Long usersId;

    /**
     * ENUM형태("IMAGE")
     */
    private AttachMentsType attachmentsType;

    /**
     * 같이 씀 - 파일 및 이미지 오브젝트 키(S3)
     */
    private String objectKey;

    /**
     * 같이 씀 - 파일 및 이미지 미리보기 및 다운로드 경로
     */
    private String presignedUrl;

    /**
     * 같이 씀 - 파일 및 이미지 확장자
     */
    private String attachmentsExtension;

    /**
     * 같이 씀 - 파일 및 이미지 DATA 크기
     */
    private Double attachmentsSize;

    /**
     * attachments_type이 IMAGE일 경우 - 이미지의 넓이
     */
    private Double imagesWidth;

    /**
     * attachments_type이 IMAGE일 경우 - 이미지의 높이
     */
    private Double imagesHeight;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public AttachMentsImagesGetInFoldersResponse(Long attachmentsId, Long usersId, AttachMentsType attachmentsType,
                                                String objectKey, String presignedUrl, String attachmentsExtension,
                                                Double attachmentsSize,  Double imagesWidth, Double imagesHeight,
                                                 LocalDateTime createdAt){
        this.attachmentsId = attachmentsId;
        this.usersId = usersId;
        this.attachmentsType = attachmentsType;
        this.objectKey = objectKey;
        this.presignedUrl = presignedUrl;
        this.attachmentsExtension = attachmentsExtension;
        this.attachmentsSize = attachmentsSize;
        this.imagesWidth = imagesWidth;
        this.imagesHeight = imagesHeight;
        this.createdAt = createdAt;
    }

    public AttachMentsImagesGetInFoldersResponse(AttachMents attachments){
        this.attachmentsId = attachments.getAttachmentsId();
        this.usersId = attachments.getUsers().getUsersId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = attachments.getPresignedUrl();
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.imagesWidth = attachments.getImagesWidth();
        this.imagesHeight = attachments.getImagesHeight();
        this.createdAt = attachments.getCreatedAt();
    }
}
