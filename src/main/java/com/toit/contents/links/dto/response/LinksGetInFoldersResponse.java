package com.toit.contents.links.dto.response;

import com.toit.contents.links.Links;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksGetInFoldersResponse {
    private Long linksId;
    /**
     * 링크 제목
     */
    private String linksName;

    /**
     * 링크 URL
     */
    private String linksUrl;

    /**
     * 링크 썸네일
     */
    private String linksThumbnail;

    /**
     * 링크 본문
     */
    private String textContent;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public LinksGetInFoldersResponse(Long linksId, String linksName, String linksUrl, String textContent, String linksThumbnail, LocalDateTime createdAt){
        this.linksId = linksId;
        this.linksName = linksName;
        this.linksUrl = linksUrl;
        this.textContent = textContent;
        this.linksThumbnail = linksThumbnail;
        this.createdAt = createdAt;
    }

    public LinksGetInFoldersResponse(Links links){
        this.linksId = links.getLinksId();
        this.linksName = links.getLinksName();
        this.linksUrl = links.getLinksUrl();
        this.linksThumbnail = links.getLinksThumbnail();
        this.textContent = links.getTextContent();
        this.createdAt = links.getCreatedAt();
    }
}
