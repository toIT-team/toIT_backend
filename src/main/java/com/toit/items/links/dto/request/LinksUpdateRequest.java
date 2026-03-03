package com.toit.items.links.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksUpdateRequest {
    private Long usersId;
    private Long foldersId;
    private Long linksId;
    private String linksName;
    private String textContent;

    public LinksUpdateRequest(Long usersId, Long foldersId, Long linksId, String linksName, String textContent) {
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.linksId = linksId;
        this.linksName = linksName;
        this.textContent = textContent;
    }
}
