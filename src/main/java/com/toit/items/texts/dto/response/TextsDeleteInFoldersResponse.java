package com.toit.items.texts.dto.response;

import com.toit.common.enums.EntityStatus;
import com.toit.items.texts.Texts;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsDeleteInFoldersResponse {
    /** 보관함 ID - 저장할 보관함 ID*/
    private Long foldersId;

    /** 텍스트 Id */
    private Long textsId;

    private String textContent;

    private EntityStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public TextsDeleteInFoldersResponse(Texts texts){
        this.foldersId = texts.getStorageId();
        this.textsId = texts.getTextsId();
        this.textContent = texts.getTextContent();
        this.status = texts.getStatus();
        this.createdAt = texts.getCreatedAt();
        this.deletedAt = texts.getDeletedAt();
    }
}