package com.toit.folders.dto.response;

import com.toit.common.enums.EntityStatus;
import com.toit.folders.Folders;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersDeleteResponse {
    private Long foldersId;
    private Long usersId;
    private String name;
    private String memo;
    private Boolean isDefault;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private EntityStatus status;
    private Boolean isFavorite;
    private Integer iconIdx;

    public FoldersDeleteResponse(Folders folders) {
        this.foldersId = folders.getFoldersId();
        this.usersId = folders.getUsers().getUsersId();
        this.name = folders.getName();
        this.memo = folders.getMemo();
        this.isDefault = folders.getIsDefault();
        this.color = folders.getColor();
        this.isFavorite = folders.getIsFavorite();
        this.createdAt = folders.getCreatedAt();
        this.iconIdx = folders.getIconIdx();
        this.deletedAt = folders.getDeletedAt();
        this.status = folders.getStatus();
    }

}
