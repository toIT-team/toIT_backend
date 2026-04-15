package com.toit.view.pageschedules.dto.response;

import com.toit.schedulesalarm.dto.response.SchedulesAlarmViewResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSchedulesAlarmViewResponse {

    private List<SchedulesAlarmViewResponse> schedulesResponses;

    public PageSchedulesAlarmViewResponse(List<SchedulesAlarmViewResponse> schedulesResponses) {
        this.schedulesResponses = schedulesResponses;
    }
}