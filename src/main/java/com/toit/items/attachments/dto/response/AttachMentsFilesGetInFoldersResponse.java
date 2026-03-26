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

    private Long usersId;

    private Long foldersId;

    /**
     * ENUM형태("FILE")
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
     * attachments_type이 FILE일 경우 - 파일의 이름
     * 길이 제한 255
     */
    private String fileName;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    /**
     * 보관함 이름
     */
    private String foldersName;

    /**
     * 생성시간 기준 서울 시간 요일 (예: 월요일)
     */
    private String dayOfWeek;

    public AttachMentsFilesGetInFoldersResponse(Long attachmentsId, Long usersId, AttachMentsType attachmentsType,
                                                String objectKey, String presignedUrl, String attachmentsExtension,
                                                Double attachmentsSize, String fileName, LocalDateTime createdAt,
                                                Long foldersId){
        this.attachmentsId = attachmentsId;
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.attachmentsType = attachmentsType;
        this.objectKey = objectKey;
        this.presignedUrl = presignedUrl;
        this.attachmentsExtension = attachmentsExtension;
        this.attachmentsSize = attachmentsSize;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.dayOfWeek = toDayOfWeekKorean(createdAt);
    }

    public AttachMentsFilesGetInFoldersResponse(AttachMents attachments){
        this.attachmentsId = attachments.getAttachmentsId();
        this.usersId = attachments.getUsers().getUsersId();
        this.foldersId = attachments.getStorageId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = attachments.getPresignedUrl();
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        this.createdAt = attachments.getCreatedAt();
        this.dayOfWeek = toDayOfWeekKorean(attachments.getCreatedAt());
    }

    public AttachMentsFilesGetInFoldersResponse(AttachMents attachments, String foldersName){
        this.attachmentsId = attachments.getAttachmentsId();
        this.usersId = attachments.getUsers().getUsersId();
        this.foldersId = attachments.getStorageId();
        this.attachmentsType = attachments.getAttachmentsType();
        this.objectKey = attachments.getObjectKey();
        this.presignedUrl = attachments.getPresignedUrl();
        this.attachmentsExtension = attachments.getAttachmentsExtension();
        this.attachmentsSize = attachments.getAttachmentsSize();
        this.fileName = attachments.getFileName();
        this.createdAt = attachments.getCreatedAt();
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
