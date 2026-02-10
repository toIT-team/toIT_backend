package com.toit.view.pageschedules;


import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSearchResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSelectDayViewResponse;


import java.time.LocalDate;

public interface PageSchedulesUseCase {

    PageSchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate, LocalDate endDate);

    PageSchedulesSelectDayViewResponse getSelectedDayScheduleView(Long usersId, LocalDate selectedDay);

    ScheduleViewResponse getScheduleView(Long usersId, Long schedulesId);
}
