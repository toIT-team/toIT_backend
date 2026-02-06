package com.toit.folders.dto.response;

import com.toit.folders.Folders;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersMemoResponse {
    private Long foldersId;
    private Long usersId;
    private String name;
    private String memo;
    private Boolean isDefault;
    private String color;
    private LocalDateTime createdAt;
    public FoldersMemoResponse(Folders folders) {
        this.foldersId = folders.getFoldersId();
        this.usersId = folders.getUsers().getUsersId();
        this.name = folders.getName();
        this.memo = folders.getMemo();
        this.isDefault = folders.getIsDefault();
        this.color = folders.getColor();
        this.createdAt = folders.getCreatedAt();
    }
}
