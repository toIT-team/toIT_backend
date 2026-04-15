package com.toit.view.pagehome;

import com.toit.folders.FoldersService;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.folders.dto.response.RecentFoldersResponse;
import com.toit.items.attachments.AttachMentsService;
import com.toit.items.links.LinksService;
import com.toit.items.texts.TextsService;
import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import com.toit.view.pagehome.dto.response.PageHomeFoldersItemResponse;
import com.toit.view.pagehome.dto.response.PageHomeViewResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageHomeUseCaseImpl implements PageHomeUseCase {

    private final FoldersService foldersService;
    private final SchedulesService schedulesService;
    private final TextsService textsService;
    private final LinksService linksService;
    private final AttachMentsService attachMentsService;

    /**
     * 정렬 우선순위:
     * 0 - 아직 시작 안 한 일정 (upcoming)
     * 1 - 진행 중인 일정 (in progress)
     * 2 - startTime, endTime 둘 다 null (또는 한쪽만 null)
     * 3 - 종료된 일정 (completed, 일반 일정에만 해당)
     *
     * overnight 일정(endTime < startTime) 처리:
     * - now >= startTime 또는 now <= endTime → 진행 중 (1)
     * - now > endTime && now < startTime → 아직 시작 안 함 (0)
     */
    private int scheduleSortOrder(SchedulesSelectedDayResponse s, LocalTime now) {
        LocalTime start = s.getStartTime();
        LocalTime end = s.getEndTime();

        if (start == null || end == null) return 2;  // 둘 다 null 또는 한쪽만 null

        boolean isOvernight = end.isBefore(start);   // ex) start=23:00, end=01:00

        if (isOvernight) {
            // 진행 중: now >= start 또는 now <= end
            if (!now.isBefore(start) || !now.isAfter(end)) return 1;
            // 그 외(now > end && now < start): 아직 시작 안 함
            return 0;
        } else {
            if (end.isBefore(now)) return 3;    // 종료된 일정
            if (start.isAfter(now)) return 0;   // 아직 시작 안 한 일정
            return 1;                           // 진행 중
        }
    }

    @Override
    public PageHomeViewResponse getHomeView(Long usersId, LocalDate todayDate) {
        // 최근 본 보관함 조회 - lastViewedAt 내림차순 최대 4개
        List<RecentFoldersResponse> foldersViews = foldersService.getRecentFolders(usersId);

        // 보관함 전체 조회 + 각 보관함 자료 개수 합산
        List<FoldersItemResponse> folders = foldersService.getAllFoldersByUser(usersId);
        List<PageHomeFoldersItemResponse> foldersWithCount = new ArrayList<>();
        for (FoldersItemResponse folder : folders) {
            long itemsCount = textsService.countByFoldersId(folder.getFoldersId())
                    + linksService.countByFoldersId(folder.getFoldersId())
                    + attachMentsService.countByFoldersId(folder.getFoldersId());
            foldersWithCount.add(new PageHomeFoldersItemResponse(folder, itemsCount));
        }

        // 오늘 일정 조회
        List<SchedulesSelectedDayResponse> schedules = schedulesService.getSelectedDaySchedules(usersId, todayDate);

        // 서울 시간 기준 현재 시간으로 정렬
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));
        List<SchedulesSelectedDayResponse> sortedSchedules = schedules.stream()
                .sorted(Comparator.comparingInt(s -> scheduleSortOrder(s, now)))
                .toList();

        return new PageHomeViewResponse(foldersWithCount, foldersViews, sortedSchedules);
    }
}