package com.toit.foldersview.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentFoldersRequest {
    /**
     * 사용자 ID
     */
    private Long usersId;
}
