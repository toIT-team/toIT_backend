package com.toit.items.attachments.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsDeleteInFoldersResponse {

    private Long attachmentsId;
    private Long usersId;

    public AttachMentsDeleteInFoldersResponse(Long attachmentsId, Long usersId){
        this.attachmentsId = attachmentsId;
        this.usersId = usersId;
    }

}
