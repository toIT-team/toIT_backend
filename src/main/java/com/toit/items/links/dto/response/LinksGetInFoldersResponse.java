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