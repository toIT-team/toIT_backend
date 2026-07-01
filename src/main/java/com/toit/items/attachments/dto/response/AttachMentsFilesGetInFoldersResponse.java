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

    /**
     * DTO 프로젝션 전용 생성자 (N+1 제거 - JOIN으로 보관함 이름까지 한 번에 조회)
     * select 컬럼 순서/타입과 정확히 일치해야 함.
     */
    public AttachMentsFilesGetInFoldersResponse(
            Long attachmentsId, Long foldersId, AttachMentsType attachmentsType,
            String objectKey, String attachmentsExtension, Double attachmentsSize,
            String fileName, LocalDateTime createdAt, String foldersName, String textContent) {
        this.attachmentsId = attachmentsId;
        this.foldersId = foldersId;
        this.attachmentsType = attachmentsType;
        this.objectKey = objectKey;
        this.presignedUrl = null;                 // 서명은 목록에서 제거했으므로 null
        this.attachmentsExtension = attachmentsExtension;
        this.attachmentsSize = attachmentsSize;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.foldersName = foldersName;
        this.textContent = textContent;
        this.dayOfWeek = toDayOfWeekKorean(createdAt);
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