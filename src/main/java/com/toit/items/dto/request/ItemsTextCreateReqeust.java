package com.toit.items.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemsTextCreateReqeust {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    /** 텍스트 제목 - 필요 없을 수도 있음*/
    private String name;

    /** 텍스트 내용 */
    private String textContent;

    public ItemsTextCreateReqeust(Long usersId, List<Long> foldersIdList, String name, String textContent){
        this.usersId = usersId;
        this.foldersIdList = foldersIdList;
        this.name = name;
        this.textContent = textContent;
    }

}
