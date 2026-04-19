package com.toit.folders.dto.response;

import com.toit.folders.Folders;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersFavoriteResponse {
    private Long foldersId;
    private Boolean isFavorite;

    public FoldersFavoriteResponse(Folders folders) {
        this.foldersId = folders.getFoldersId();
        this.isFavorite = folders.getIsFavorite();
    }
}
