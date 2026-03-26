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

    private Long usersId;

    private Long foldersId;
    /**
     * 메모 본문
     */
    private String textContent;

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

    public TextsGetInFoldersResponse(Texts texts, Long usersId){
        this.textsId = texts.getTextsId();
        this.usersId = usersId;
        this.foldersId = texts.getStorageId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
        this.dayOfWeek = toDayOfWeekKorean(texts.getCreatedAt());
    }

    public TextsGetInFoldersResponse(Texts texts, Long usersId, String foldersName){
        this.textsId = texts.getTextsId();
        this.usersId = usersId;
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
