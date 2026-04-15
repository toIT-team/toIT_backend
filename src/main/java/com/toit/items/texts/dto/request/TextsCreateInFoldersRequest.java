package com.toit.items.texts.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsCreateInFoldersRequest {
    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    /** 텍스트 내용 */
    private String textContent;

    public TextsCreateInFoldersRequest(List<Long> foldersIdList, String textContent){
        this.foldersIdList = foldersIdList;
        this.textContent = textContent;
    }
}
