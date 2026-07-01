package com.toit.items.texts.dto.response;

import com.toit.items.texts.Texts;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsGetInFoldersResponse {
    private Long textsId;

    private Long foldersId;

    private String textContent;

    private LocalDateTime createdAt;

    private String foldersName;

    private String dayOfWeek;

    public TextsGetInFoldersResponse(Texts texts){
        this.textsId = texts.getTextsId();
        this.foldersId = texts.getStorageId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
        this.dayOfWeek = toDayOfWeekKorean(texts.getCreatedAt());
    }

    public TextsGetInFoldersResponse(Texts texts, String foldersName){
        this.textsId = texts.getTextsId();
        this.foldersId = texts.getStorageId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
        this.foldersName = foldersName;
        this.dayOfWeek = toDayOfWeekKorean(texts.getCreatedAt());
    }

    public TextsGetInFoldersResponse(Long textsId, String textContent, LocalDateTime createdAt){
        this.textsId = textsId;
        this.textContent = textContent;
        this.createdAt = createdAt;
        this.dayOfWeek = toDayOfWeekKorean(createdAt);
    }

    /**
     * DTO 프로젝션 전용 생성자 (N+1 제거 - JOIN으로 보관함 이름까지 한 번에 조회)
     * select 컬럼 순서/타입과 정확히 일치해야 함.
     */
    public TextsGetInFoldersResponse(
            Long textsId, Long foldersId, String textContent, LocalDateTime createdAt, String foldersName){
        this.textsId = textsId;
        this.foldersId = foldersId;
        this.textContent = textContent;
        this.createdAt = createdAt;
        this.foldersName = foldersName;
        this.dayOfWeek = toDayOfWeekKorean(createdAt);
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