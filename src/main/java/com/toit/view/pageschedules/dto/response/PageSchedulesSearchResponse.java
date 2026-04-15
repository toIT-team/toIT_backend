package com.toit.view.pageschedules.dto.response;

import com.toit.schedules.dto.response.SchedulesMonthResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSchedulesSearchResponse {

    private List<SchedulesMonthResponse> schedulesResponses;

    public PageSchedulesSearchResponse(List<SchedulesMonthResponse> schedulesResponses) {
        this.schedulesResponses = schedulesResponses;
    }
}