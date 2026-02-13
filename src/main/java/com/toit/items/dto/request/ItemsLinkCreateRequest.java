package com.toit.items.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemsLinkCreateRequest {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    /** 링크 URL */
    private String filePath;


    public ItemsLinkCreateRequest(Long usersId, List<Long> foldersIdList, String filePath){
        this.usersId = usersId;
        this.foldersIdList = foldersIdList;
        this.filePath = filePath;
    }
}
