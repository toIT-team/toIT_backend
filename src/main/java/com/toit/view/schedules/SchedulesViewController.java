package com.toit.view.schedules;


import com.toit.schedules.dto.request.ScheduleViewRequest;
import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.view.schedules.dto.request.ScheduleSearchRequest;
import com.toit.view.schedules.dto.request.SchedulesSelectDayRequest;
import com.toit.view.schedules.dto.response.SchedulesSearchResponse;
import com.toit.view.schedules.dto.response.SchedulesSelectDayViewResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class SchedulesViewController {

    private final SchedulesUseCase schedulesUseCase;

    /**
     * startDate ~ endDate 내의 일정 조회
     */
    @GetMapping("/search")
    public ResponseEntity<SchedulesSearchResponse> getSearchSchedules(
            @RequestBody ScheduleSearchRequest request
    ) {
        return ResponseEntity.ok(schedulesUseCase.
                getSearchSchedulesView(
                        request.getUserId(),
                        request.getStartDate(),
                        request.getEndDate()));
    }

    /**
     * 선택한 날짜에 해당하는 일정 조회
     */
    @GetMapping("/selected")
    public ResponseEntity<SchedulesSelectDayViewResponse> getSelectedDaySchedules(
            @RequestBody SchedulesSelectDayRequest request
            ){

        return ResponseEntity.ok(schedulesUseCase.
                getSelectedDayScheduleView(
                        request.getUsersId(),
                        request.getSelectedDay()));

    }

    @GetMapping("/detail")
    public ResponseEntity<ScheduleViewResponse> getSchedule(
            @RequestBody ScheduleViewRequest request
            ){

        return ResponseEntity.ok(schedulesUseCase.
                getScheduleView(
                        request.getUsersId(),
                        request.getSchedulesId()));

    }
}
