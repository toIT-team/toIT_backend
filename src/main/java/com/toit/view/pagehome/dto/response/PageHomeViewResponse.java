package com.toit.view.pagehome.dto.response;

import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.foldersview.dto.response.RecentFoldersResponse;
import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageHomeViewResponse {
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

    /**
     * 오늘 일정 조회
     */
    private List<SchedulesSelectedDayResponse> schedules;

    public PageHomeViewResponse(Long userId, List<FoldersItemResponse> folders, List<RecentFoldersResponse> foldersViews
            , List<SchedulesSelectedDayResponse> schedules) {
        this.userId = userId;
        this.folders = folders;
        this.foldersViews = foldersViews;
        this.schedules = schedules;
    }
}
