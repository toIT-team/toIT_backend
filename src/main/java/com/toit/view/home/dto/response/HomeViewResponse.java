package com.toit.view.home.dto.response;

import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.foldersview.dto.response.RecentFoldersResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeViewResponse {
    /**
     * 사용자의 보관함 조회
     */
    private Long userId;

    /**
     * 사용자의 보관함 모음
     */
    private List<FoldersItemResponse> folders;

    /**
     * 사용자의 최근 본 폴더 조회 - 최대 4개
     */
    private List<RecentFoldersResponse> foldersViews;


    public HomeViewResponse(Long userId, List<FoldersItemResponse> folders, List<RecentFoldersResponse> foldersViews) {
        this.userId = userId;
        this.folders = folders;
        this.foldersViews = foldersViews;
    }
}
