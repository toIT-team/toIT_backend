package com.toit.items.texts.dto.response;

import com.toit.items.texts.Texts;
import java.time.LocalDateTime;

public class TextsGetInFoldersResponse {
    private Long textsId;
    /**
     * 메모 본문
     */
    private String textContent;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public TextsGetInFoldersResponse(Texts texts){
        this.textsId = texts.getTextsId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
    }

    public TextsGetInFoldersResponse(Long textsId, String textContent, LocalDateTime createdAt){
        this.textsId = textsId;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }

}
