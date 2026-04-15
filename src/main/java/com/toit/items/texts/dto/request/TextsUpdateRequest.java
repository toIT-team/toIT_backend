package com.toit.items.texts.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsUpdateRequest {
    private Long foldersId;
    private Long textsId;
    private String textContent;

    public TextsUpdateRequest(Long foldersId, Long textsId, String textContent) {
        this.foldersId = foldersId;
        this.textsId = textsId;
        this.textContent = textContent;
    }
}