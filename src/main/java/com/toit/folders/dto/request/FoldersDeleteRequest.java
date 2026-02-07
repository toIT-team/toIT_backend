package com.toit.folders.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersDeleteRequest {
    private Long usersId;
    private Long foldersId;

    public FoldersDeleteRequest(Long usersId, Long foldersId){
        this.usersId = usersId;
        this.foldersId = foldersId;
    }
}

