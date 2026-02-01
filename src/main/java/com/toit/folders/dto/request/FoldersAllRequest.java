package com.toit.folders.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersAllRequest {
    /**
     * 사용자 ID
     * */
    private Long usersId;

    public FoldersAllRequest(Long usersId){
        this.usersId = usersId;
    }
}
