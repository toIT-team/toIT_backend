package com.toit.items.links.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksMoveInFoldersRequest {
    private Long foldersId;

    private Long moveFoldersId;

    private Long linksId;

    public LinksMoveInFoldersRequest(Long moveFoldersId, Long foldersId, Long linksId){
        this.moveFoldersId = moveFoldersId;
        this.foldersId = foldersId;
        this.linksId = linksId;
    }
}
