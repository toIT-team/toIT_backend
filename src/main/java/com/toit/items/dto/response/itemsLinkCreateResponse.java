package com.toit.items.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class itemsLinkCreateResponse {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    private String filePath;

    /** 텍스트 내용 */
    private String textContent;

    private String linkThumbnail;

    private String name;



    public itemsLinkCreateResponse(Long usersId, List<Long> foldersIdList, String filePath, String textContent, String linkThumbnail ,String name){
        this.usersId = usersId;
        this.foldersIdList = foldersIdList;
        this.filePath = filePath;
        this.textContent = textContent;
        this.linkThumbnail = linkThumbnail;
        this.name = name;
    }
}
