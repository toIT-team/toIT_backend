package com.toit.items.texts.dto.response;

import com.toit.items.texts.Texts;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsUpdateResponse {
    private Long textsId;
    private Long foldersId;
    private String textContent;
    private LocalDateTime createdAt;

    public TextsUpdateResponse(Texts texts) {
        this.textsId = texts.getTextsId();
        this.foldersId = texts.getStorageId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
    }
}
