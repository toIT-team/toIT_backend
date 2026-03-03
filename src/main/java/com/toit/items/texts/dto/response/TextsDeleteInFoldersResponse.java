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
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID*/
    private Long foldersId;

    /** 텍스트 Id */
    private Long textsId;

    private String textContent;

    private EntityStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
    public TextsDeleteInFoldersResponse(Long usersId, Long storageId, Long textsId,
                                        String textContent, EntityStatus status, LocalDateTime createdAt,
                                        LocalDateTime deletedAt){
        this.usersId = usersId;
        this.foldersId = storageId;
        this.textsId = textsId;
        this.textContent = textContent;
        this.status = status;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public TextsDeleteInFoldersResponse(Texts texts){
        this.usersId = texts.getUsers().getUsersId();
        this.foldersId = texts.getStorageId();
        this.textsId = texts.getTextsId();
        this.textContent = texts.getTextContent();
        this.status = texts.getStatus();
        this.createdAt = texts.getCreatedAt();
        this.deletedAt = texts.getDeletedAt();
    }
}
