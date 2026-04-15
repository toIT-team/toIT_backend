package com.toit.folders.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersUpdateRequest {
    private Long foldersId;
    private String name;
    private String memo;
    private String color;
    private Integer iconIdx;

    public FoldersUpdateRequest(Long foldersId, String name, String memo, String color, Integer iconIdx){
        this.foldersId = foldersId;
        this.name = name;
        this.memo = memo;
        this.color = color;
        this.iconIdx = iconIdx;
    }
}
