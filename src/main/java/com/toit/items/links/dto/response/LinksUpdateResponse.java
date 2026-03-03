package com.toit.items.links.dto.response;

import com.toit.items.links.Links;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksUpdateResponse {
    private Long usersId;
    private Long foldersId;
    private Long linksId;
    private String linksName;
    private String linksUrl;
    private String linksThumbnail;
    private String textContent;

    public LinksUpdateResponse(Links links) {
        this.usersId = links.getUsers().getUsersId();
        this.foldersId = links.getStorageId();
        this.linksId = links.getLinksId();
        this.linksName = links.getLinksName();
        this.linksUrl = links.getLinksUrl();
        this.linksThumbnail = links.getLinksThumbnail();
        this.textContent = links.getTextContent();
    }
}
