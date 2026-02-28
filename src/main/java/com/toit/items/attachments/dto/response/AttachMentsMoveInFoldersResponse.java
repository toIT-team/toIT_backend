package com.toit.items.attachments.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsMoveInFoldersResponse {
    private Long usersId;

    private Long foldersId;

    private Long moveFoldersId;

    private Long attachmentsId;

    public AttachMentsMoveInFoldersResponse(Long usersId, Long moveFoldersId, Long foldersId, Long attachmentsId){
        this.usersId = usersId;
        this.moveFoldersId = moveFoldersId;
        this.foldersId = foldersId;
        this.attachmentsId = attachmentsId;
    }
}