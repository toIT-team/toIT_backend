package com.toit.items.links.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksCreateInFoldersRequest {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    /** 링크 URL */
    private String linksUrl;

    public LinksCreateInFoldersRequest(Long usersId, List<Long> foldersIdList, String linksUrl){
        this.usersId = usersId;
        this.foldersIdList = foldersIdList;
        this.linksUrl = linksUrl;
    }
}
