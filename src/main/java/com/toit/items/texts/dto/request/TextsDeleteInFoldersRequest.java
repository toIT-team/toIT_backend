package com.toit.items.texts.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsDeleteInFoldersRequest {

    /** 보관함 ID */
    private Long foldersId;

    /** 텍스트 Id */
    private Long textsId;

    public TextsDeleteInFoldersRequest(Long foldersId, Long textsId){
        this.foldersId = foldersId;
        this.textsId = textsId;
    }
}