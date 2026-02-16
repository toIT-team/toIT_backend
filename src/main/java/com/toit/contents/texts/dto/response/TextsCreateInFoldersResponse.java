package com.toit.contents.texts.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsCreateInFoldersResponse {
    private Long textsId;

    private List<Long> foldersIdList;

    /**
     * 메모 본문
     */
    private String textContent;


    public TextsCreateInFoldersResponse(Long textsId, List<Long> foldersIdList, String textContent){
        this.textsId = textsId;
        this.foldersIdList = foldersIdList;
        this.textContent = textContent;
    }

}
