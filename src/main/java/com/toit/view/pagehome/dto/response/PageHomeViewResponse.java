package com.toit.view.pagehome.dto.response;

import com.toit.foldersviews.dto.response.RecentFoldersResponse;
import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageHomeViewResponse {

    private List<PageHomeFoldersItemResponse> folders;
    private List<RecentFoldersResponse> foldersViews;
    private List<SchedulesSelectedDayResponse> schedules;

    public PageHomeViewResponse(List<PageHomeFoldersItemResponse> folders, List<RecentFoldersResponse> foldersViews,
                                List<SchedulesSelectedDayResponse> schedules) {
        this.folders = folders;
        this.foldersViews = foldersViews;
        this.schedules = schedules;
    }
}