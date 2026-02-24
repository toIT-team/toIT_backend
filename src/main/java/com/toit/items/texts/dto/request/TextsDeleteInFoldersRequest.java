package com.toit.items.texts.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsDeleteInFoldersRequest {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID*/
    private Long foldersId;

    /** 텍스트 Id */
    private Long textsId;

    public TextsDeleteInFoldersRequest(Long usersId, Long foldersId, Long textsId){
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.textsId = textsId;
    }

}
