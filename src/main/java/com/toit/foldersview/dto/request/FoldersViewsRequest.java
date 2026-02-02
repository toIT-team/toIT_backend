package com.toit.foldersview.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersViewsRequest {

    /**
     * 사용자 ID
     * */
    private Long usersId;

    /**
     * 보관함 ID
     */
    private Long foldersId;

    public FoldersViewsRequest(Long usersId, Long foldersId){
        this.usersId = usersId;
        this.foldersId = foldersId;
    }
}
