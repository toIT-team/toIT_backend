package com.toit.items.links.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksDeleteInFoldersRequest {

    private Long usersId;

    private Long foldersId;

    private Long linksId;

    public LinksDeleteInFoldersRequest(Long usersId, Long foldersId, Long linksId){
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.linksId = linksId;
    }
}
