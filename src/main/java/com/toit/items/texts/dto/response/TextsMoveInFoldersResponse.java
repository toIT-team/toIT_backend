package com.toit.items.texts.dto.response;

import com.toit.items.texts.Texts;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextsMoveInFoldersResponse {
    private Long usersId;

    private Long textsId;

    private Long foldersId;
    /**
     * 메모 본문
     */
    private String textContent;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public TextsMoveInFoldersResponse(Texts texts){
        this.usersId = texts.getUsers().getUsersId();
        this.foldersId = texts.getStorageId();
        this.textsId = texts.getTextsId();
        this.textContent = texts.getTextContent();
        this.createdAt = texts.getCreatedAt();
    }

}
