package com.toit.items.links.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksCreateInFoldersRequest {
    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    /** 링크 URL */
    private String linksUrl;

    private String linksName;

    private String textContent;

    private String linksThumbnail;

    public LinksCreateInFoldersRequest(List<Long> foldersIdList, String linksUrl, String linksName, String textContent, String linksThumbnail){
        this.foldersIdList = foldersIdList;
        this.linksUrl = linksUrl;
        this.linksName = linksName;
        this.textContent = textContent;
        this.linksThumbnail = linksThumbnail;
    }
}
