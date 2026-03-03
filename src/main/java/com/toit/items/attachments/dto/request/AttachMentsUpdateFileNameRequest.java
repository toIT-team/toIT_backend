package com.toit.items.attachments.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsUpdateFileNameRequest {
    private Long usersId;
    private Long foldersId;
    private Long attachmentsId;
    private String fileName;

    public AttachMentsUpdateFileNameRequest(Long usersId, Long foldersId, Long attachmentsId, String fileName) {
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.attachmentsId = attachmentsId;
        this.fileName = fileName;
    }
}
