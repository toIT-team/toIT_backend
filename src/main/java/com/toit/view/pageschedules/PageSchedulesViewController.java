package com.toit.view.pageschedules;


import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.swagger.docs.schedules.SchedulesAlarmApiDocs;
import com.toit.swagger.docs.schedules.SchedulesApiDocs;
import com.toit.view.pageschedules.dto.response.PageSchedulesAlarmViewResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSearchResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSelectDayViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Pages - Schedules", description = "일정 화면(page) 전용 API")
@RestController
@RequestMapping("/page/schedules")
@RequiredArgsConstructor
public class PageSchedulesViewController {

    private final PageSchedulesUseCase schedulesUseCase;

    /**
     * startDate ~ endDate 내의 일정 조회
     */
    @Operation(
            summary = "Schedules 화면 API - 화면이름 : 캘린더 - 홈"
    )
    @SchedulesApiDocs
    @GetMapping("/search")
    public ResponseEntity<PageSchedulesSearchResponse> getSearchSchedules(
            @RequestParam("usersId") Long usersId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        return ResponseEntity.ok(schedulesUseCase.
                getSearchSchedulesView(
                        usersId,
                        startDate,
                        endDate));
    }


    /**
     * 선택한 날짜에 해당하는 일정 조회
     */
    @Operation(
            summary = "Schedules 화면 API - 화면이름 : 캘린더 - 일정 , 홈-일정"
    )
    @SchedulesApiDocs
    @GetMapping("/selected")
    public ResponseEntity<PageSchedulesSelectDayViewResponse> getSelectedDaySchedules(
            @RequestParam("usersId") Long usersId,
            @RequestParam("selectedDay") LocalDate selectedDay

            ){

        return ResponseEntity.ok(schedulesUseCase.
                getSelectedDaySchedulesView(
                        usersId,
                        selectedDay));

    }

    @Operation(
            summary = "Schedules 화면 API - 화면이름 : 캘린더 - 일정 상세"
    )
    @SchedulesApiDocs
    @GetMapping
    public ResponseEntity<ScheduleViewResponse> getSchedule(
            @RequestParam("usersId") Long usersId,
            @RequestParam("schedulesId") Long schedulesId
            ){

        return ResponseEntity.ok(schedulesUseCase.
                getScheduleView(
                        usersId,
                        schedulesId));

    }

    /***
     * 전송된 푸시알림 리스트 조회
     */
    @Operation(summary = "SchedulesAlarm 화면 APO - 화면이름 : 알림 ")
    @SchedulesAlarmApiDocs
    @GetMapping("/alarm")
    public ResponseEntity<PageSchedulesAlarmViewResponse> getScheduleAlarms(
            @RequestParam("usersId") Long usersId
    ){
        PageSchedulesAlarmViewResponse response = schedulesUseCase.getSchedulesAlarmsView(usersId);

        return ResponseEntity.ok(response);

    }
}
