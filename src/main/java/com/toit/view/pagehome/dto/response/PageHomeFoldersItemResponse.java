package com.toit.view.pagehome.dto.response;

import com.toit.folders.dto.response.FoldersItemResponse;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageHomeFoldersItemResponse {
    private Long foldersId;
    private String name;
    private String memo;
    private Boolean isDefault;
    private String color;
    private LocalDateTime createdAt;
    private Boolean isFavorite;
    private Integer iconIdx;
    private Long itemsCount;

    public PageHomeFoldersItemResponse(FoldersItemResponse folder, long itemsCount) {
        this.foldersId = folder.getFoldersId();
        this.name = folder.getName();
        this.memo = folder.getMemo();
        this.isDefault = folder.getIsDefault();
        this.color = folder.getColor();
        this.createdAt = folder.getCreatedAt();
        this.isFavorite = folder.getIsFavorite();
        this.iconIdx = folder.getIconIdx();
        this.itemsCount = itemsCount;
    }
}