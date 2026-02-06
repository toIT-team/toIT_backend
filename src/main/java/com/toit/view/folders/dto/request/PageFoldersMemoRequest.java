package com.toit.view.folders.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageFoldersMemoRequest {
    private Long usersId;
    private Long foldersId;

    public PageFoldersMemoRequest(Long foldersId, Long usersId) {
        this.foldersId = foldersId;
        this.usersId = usersId;
    }
}