package com.toit.view.schedules;


import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.view.schedules.dto.response.SchedulesSearchResponse;
import com.toit.view.schedules.dto.response.SchedulesSelectDayViewResponse;


import java.time.LocalDate;

public interface SchedulesUseCase {

    SchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate, LocalDate endDate);

    SchedulesSelectDayViewResponse getSelectedDayScheduleView(Long usersId, LocalDate selectedDay);

    ScheduleViewResponse getScheduleView(Long usersId, Long schedulesId);
}
