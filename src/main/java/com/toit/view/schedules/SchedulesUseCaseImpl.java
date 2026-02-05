package com.toit.view.schedules;


import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.schedules.dto.response.SchedulesMonthResponse;
import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import com.toit.view.schedules.dto.response.SchedulesSearchResponse;
import com.toit.view.schedules.dto.response.SchedulesSelectDayViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulesUseCaseImpl implements SchedulesUseCase{

    private final SchedulesService schedulesService;

    /***
     * startDate ~ endDate 범위 내의 일정들을 반환
     */
    @Override
    public SchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate, LocalDate endDate) {

        List<SchedulesMonthResponse> schedules = schedulesService.getSearchSchedules(usersId, startDate, endDate);

        return new SchedulesSearchResponse(usersId, schedules);
    }

    /***
     * 선택된 날짜의 스케줄들을 반환
     */
    @Override
    public SchedulesSelectDayViewResponse getSelectedDayScheduleView(Long userId, LocalDate selectedDay){
        List<SchedulesSelectedDayResponse> schedules = schedulesService.getSelectedDaySchedules(userId, selectedDay);

        return  new SchedulesSelectDayViewResponse(userId, schedules) ;

    }

    @Override
    public ScheduleViewResponse getScheduleView(Long usersId, Long schedulesId){
        ScheduleViewResponse schedule = schedulesService.getSchedule(usersId, schedulesId);

        return new ScheduleViewResponse(
                schedule.getUserId(),
                schedule.getSchedulesId(),
                schedule.getTitle(),
                schedule.getFoldersId(),
                schedule.getFoldersTitle(),
                schedule.getTimeSetting(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getLocation(),
                schedule.getNotification(),
                schedule.getMemo()
        );
    }


}
