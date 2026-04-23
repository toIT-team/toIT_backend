package com.toit.folders.dto.response;

import com.toit.folders.Folders;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersItemResponse {
    private Long foldersId;
    private String name;
    private String memo;
    private Boolean isDefault;
    private String color;
    private LocalDateTime createdAt;
    private Boolean isFavorite;
    private Integer iconIdx;

    public FoldersItemResponse(Folders folders) {
        this.foldersId = folders.getFoldersId();
        this.name = folders.getName();
        this.memo = folders.getMemo();
        this.isDefault = folders.getIsDefault();
        this.color = folders.getColor();
        this.isFavorite = folders.getIsFavorite();
        this.createdAt = folders.getCreatedAt();
        this.iconIdx = folders.getIconIdx();
    }
}
