package com.toit.view.pagehome;

import com.toit.folders.FoldersService;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.foldersview.FoldersViewsService;
import com.toit.foldersview.dto.response.RecentFoldersResponse;
import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import com.toit.view.pagehome.dto.response.PageHomeViewResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagePageHomeUseCaseImpl implements PageHomeUseCase {

    private final FoldersViewsService foldersViewsService;
    private final FoldersService foldersService;
    private final SchedulesService schedulesService;

    @Override
    public PageHomeViewResponse getHomeView(Long usersId, LocalDate todayDate) {
        // 최근 본 보관함 조회 - 시간 내림차순으로 정렬 후 최대 4개
        List<RecentFoldersResponse> foldersViews = foldersViewsService.getRecentFolders(usersId);

        // 보관함 전체 조회
        List<FoldersItemResponse> folders = foldersService.getAllFoldersByUser(usersId);

        // 오늘 일정 조회
        List<SchedulesSelectedDayResponse> schedules = schedulesService.getSelectedDaySchedules(usersId, todayDate);

        return new PageHomeViewResponse(usersId, folders, foldersViews, schedules);
    }
}