package com.toit.items.attachments.dto.response;

import com.toit.common.enums.AttachMentsType;
import com.toit.items.attachments.AttachMents;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsFilesGetInFoldersResponse {
    private Long attachmentsId;
    private Long foldersId;
    private AttachMentsType attachmentsType;
    private String objectKey;
    private String presignedUrl;
    private String attachmentsExtension;
    private Double attachmentsSize;
    private String fileName;
    private LocalDateTime createdAt;
    private String foldersName;
    private String textContent;
    private String dayOfWeek;

    public AttachMentsFilesGetInFoldersResponse(AttachMents attachments, String signedUrl) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.foldersId = attachments.getStorageId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = signedUrl;
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        this.textContent = attachments.getTextContent();
        this.createdAt = attachments.getCreatedAt();
        this.dayOfWeek = toDayOfWeekKorean(attachments.getCreatedAt());
    }

    public AttachMentsFilesGetInFoldersResponse(AttachMents attachments, String foldersName, String signedUrl) {
        this.attachmentsId = attachments.getAttachmentsId();
        this.foldersId = attachments.getStorageId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = signedUrl;
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        this.createdAt = attachments.getCreatedAt();
        this.textContent = attachments.getTextContent();
        this.foldersName = foldersName;
        this.dayOfWeek = toDayOfWeekKorean(attachments.getCreatedAt());
    }

    private static String toDayOfWeekKorean(LocalDateTime createdAt) {
        if (createdAt == null) return null;
        ZonedDateTime seoulTime = createdAt.atZone(ZoneId.of("Asia/Seoul"));
        return switch (seoulTime.getDayOfWeek()) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}