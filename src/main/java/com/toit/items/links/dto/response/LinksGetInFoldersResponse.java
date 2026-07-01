package com.toit.items.links.dto.response;

import com.toit.items.links.Links;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksGetInFoldersResponse {
    private Long linksId;

    private Long foldersId;

    private String linksName;

    private String linksUrl;

    private String linksThumbnail;

    private String textContent;

    private LocalDateTime createdAt;

    private String foldersName;

    private String dayOfWeek;

    public LinksGetInFoldersResponse(Links links){
        this.linksId = links.getLinksId();
        this.foldersId = links.getStorageId();
        this.linksName = links.getLinksName();
        this.linksUrl = links.getLinksUrl();
        this.linksThumbnail = links.getLinksThumbnail();
        this.textContent = links.getTextContent();
        this.createdAt = links.getCreatedAt();
        this.dayOfWeek = toDayOfWeekKorean(links.getCreatedAt());
    }

    /**
     * DTO 프로젝션 전용 생성자 (N+1 제거 - JOIN으로 보관함 이름까지 한 번에 조회)
     * select 컬럼 순서/타입과 정확히 일치해야 함.
     */
    public LinksGetInFoldersResponse(
            Long linksId, Long foldersId, String linksName, String linksUrl,
            String linksThumbnail, String textContent, LocalDateTime createdAt, String foldersName) {
        this.linksId = linksId;
        this.foldersId = foldersId;
        this.linksName = linksName;
        this.linksUrl = linksUrl;
        this.linksThumbnail = linksThumbnail;
        this.textContent = textContent;
        this.createdAt = createdAt;
        this.foldersName = foldersName;
        this.dayOfWeek = toDayOfWeekKorean(createdAt);
    }

    public LinksGetInFoldersResponse(Links links, String foldersName){
        this.linksId = links.getLinksId();
        this.foldersId = links.getStorageId();
        this.linksName = links.getLinksName();
        this.linksUrl = links.getLinksUrl();
        this.linksThumbnail = links.getLinksThumbnail();
        this.textContent = links.getTextContent();
        this.createdAt = links.getCreatedAt();
        this.foldersName = foldersName;
        this.dayOfWeek = toDayOfWeekKorean(links.getCreatedAt());
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