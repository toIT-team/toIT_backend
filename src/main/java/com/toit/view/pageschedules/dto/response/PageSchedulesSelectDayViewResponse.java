package com.toit.view.pageschedules.dto.response;

import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSchedulesSelectDayViewResponse {

    private List<SchedulesSelectedDayResponse> schedulesResponses;

    public PageSchedulesSelectDayViewResponse(List<SchedulesSelectedDayResponse> schedulesResponses) {
        this.schedulesResponses = schedulesResponses;
    }
}