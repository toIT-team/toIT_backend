package com.toit.items.attachments.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMenetsMoveInFoldersRequest {
    private Long usersId;

    private Long foldersId;

    private Long moveFoldersId;

    private Long attachmentsId;

    public AttachMenetsMoveInFoldersRequest(Long usersId, Long moveFoldersId, Long foldersId, Long attachmentsId){
        this.usersId = usersId;
        this.moveFoldersId = moveFoldersId;
        this.foldersId = foldersId;
        this.attachmentsId = attachmentsId;
    }
}
