package com.toit.items.attachments.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsDeleteInFoldersResponse {

    private Long attachmentsId;

    public AttachMentsDeleteInFoldersResponse(Long attachmentsId){
        this.attachmentsId = attachmentsId;
    }

}