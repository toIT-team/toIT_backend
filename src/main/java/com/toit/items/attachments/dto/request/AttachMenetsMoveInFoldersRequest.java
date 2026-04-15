package com.toit.items.attachments.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMenetsMoveInFoldersRequest {

    private Long foldersId;

    private Long moveFoldersId;

    private Long attachmentsId;

    public AttachMenetsMoveInFoldersRequest(Long moveFoldersId, Long foldersId, Long attachmentsId){
        this.moveFoldersId = moveFoldersId;
        this.foldersId = foldersId;
        this.attachmentsId = attachmentsId;
    }
}