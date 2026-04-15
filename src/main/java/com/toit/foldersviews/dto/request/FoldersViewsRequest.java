package com.toit.foldersviews.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersViewsRequest {

    /**
     * 보관함 ID
     */
    private Long foldersId;

    public FoldersViewsRequest(Long foldersId){
        this.foldersId = foldersId;
    }
}
