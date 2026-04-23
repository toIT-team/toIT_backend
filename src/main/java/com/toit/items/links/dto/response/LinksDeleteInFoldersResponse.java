package com.toit.items.links.dto.response;

import com.toit.common.enums.EntityStatus;
import com.toit.items.links.Links;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksDeleteInFoldersResponse {
    /** 보관함 ID - 저장할 보관함 ID*/
    private Long foldersId;

    /** 텍스트 Id */
    private Long linksId;

    private String linksName;

    private String linksUrl;

    private String linksThumbnail;

    private String textContent;

    private EntityStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public LinksDeleteInFoldersResponse(Links links){
        this.foldersId = links.getStorageId();
        this.linksId = links.getLinksId();
        this.linksName = links.getLinksName();
        this.linksUrl = links.getLinksUrl();
        this.linksThumbnail = links.getLinksThumbnail();
        this.textContent = links.getTextContent();
        this.status = links.getStatus();
        this.createdAt = links.getCreatedAt();
        this.deletedAt = links.getDeletedAt();
    }
}
