package com.toit.view.schedules;

import com.toit.view.schedules.dto.response.SchedulesSearchResponse;

import java.time.LocalDate;

public interface SchedulesUseCase {

    SchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate, LocalDate endDate);

}
