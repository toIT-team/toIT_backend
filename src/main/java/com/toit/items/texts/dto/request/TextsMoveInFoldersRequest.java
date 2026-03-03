package com.toit.items.texts.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsMoveInFoldersRequest {
    /** 사용자 ID */
    private Long usersId;

    private Long foldersId;

    /** 보관함 ID - 이동시킬 보관함 ID*/
    private Long moveFoldersId;

    /** 텍스트 Id */
    private Long textsId;

    public TextsMoveInFoldersRequest(Long usersId, Long foldersId, Long moveFoldersId, Long textsId){
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.moveFoldersId = moveFoldersId;
        this.textsId = textsId;
    }
}
