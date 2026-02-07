package com.toit.folders.dto.response;

import com.toit.folders.Folders;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersUpdateResponse {
    private Long usersId;
    private Long foldersId;
    private String name;
    private String memo;
    private String color;
    private Integer iconIdx;

    public FoldersUpdateResponse(Folders folders, Long usersId) {
        this.usersId = usersId;
        this.foldersId = folders.getFoldersId();
        this.name = folders.getName();
        this.memo = folders.getMemo();
        this.color = folders.getColor();
        this.iconIdx = folders.getIconIdx();
    }
}
