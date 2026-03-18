package com.toit.items.texts.dto.response;

import com.toit.items.texts.Texts;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsGetInFoldersResponse {
    private Long textsId;

    private Long usersId;

    private Long foldersId;
    /**
     * 메모 본문
     */
    private String textContent;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public TextsGetInFoldersResponse(Texts texts, Long usersId){
        this.textsId = texts.getTextsId();
        this.usersId = usersId;
        this.foldersId = texts.getStorageId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
    }

    public TextsGetInFoldersResponse(Long textsId, String textContent, LocalDateTime createdAt){
        this.textsId = textsId;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }

}
