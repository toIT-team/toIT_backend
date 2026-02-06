package com.toit.folders.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersUpdateRequest {
    private Long usersId;
    private Long foldersId;
    private String name;
    private String memo;
    private String color;
    private Integer iconIdx;

    public FoldersUpdateRequest(Long usersId, Long foldersId, String name, String memo, String color, Integer iconIdx){
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.name = name;
        this.memo = memo;
        this.color = color;
        this.iconIdx = iconIdx;
    }
}
