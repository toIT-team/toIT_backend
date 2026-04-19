package com.toit.folders.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersFavoriteRequest {
    private Long foldersId;
    private Boolean isFavorite;

    public FoldersFavoriteRequest(Long foldersId, Boolean isFavorite) {
        this.foldersId = foldersId;
        this.isFavorite = isFavorite;
    }
}