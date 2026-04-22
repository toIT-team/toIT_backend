package com.toit.items.links.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksCreateInFoldersResponse {
    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    private String linksUrl;

    private String linksName;

    private String textContent;

    private String linksThumbnail;

    public LinksCreateInFoldersResponse(List<Long> foldersIdList, String linksUrl, String textContent, String linksThumbnail, String linksName){
        this.foldersIdList = foldersIdList;
        this.linksUrl = linksUrl;
        this.textContent = textContent;
        this.linksThumbnail = linksThumbnail;
        this.linksName = linksName;
    }
}