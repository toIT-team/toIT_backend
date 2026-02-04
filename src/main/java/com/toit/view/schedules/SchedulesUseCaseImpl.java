package com.toit.view.schedules;


import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.SchedulesMonthResponse;
import com.toit.view.schedules.dto.response.SchedulesSearchResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulesUseCaseImpl implements SchedulesUseCase{

    private final SchedulesService schedulesService;

    @Override
    public SchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate,LocalDate endDate) {

        List<SchedulesMonthResponse> schedules = schedulesService.getSearchSchedules(usersId, startDate, endDate);

        return new SchedulesSearchResponse(usersId, schedules);
    }


}
