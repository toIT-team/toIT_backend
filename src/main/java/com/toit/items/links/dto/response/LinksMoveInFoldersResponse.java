package com.toit.items.links.dto.response;

import com.toit.common.enums.EntityStatus;
import com.toit.items.links.Links;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksMoveInFoldersResponse {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID*/
    private Long foldersId;

    /** 텍스트 Id */
    private Long linksId;

    private String linksName;

    private String linksUrl;

    private String linksThumbnail;

    private String textContent;

    public LinksMoveInFoldersResponse(Links links){
        this.usersId = links.getUsers().getUsersId();
        this.foldersId = links.getStorageId();
        this.linksId = links.getLinksId();
        this.linksName = links.getLinksName();
        this.linksUrl = links.getLinksUrl();
        this.linksThumbnail = links.getLinksThumbnail();
        this.textContent = links.getTextContent();
    }
}
