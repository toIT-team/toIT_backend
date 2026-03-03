package com.toit.view.pageschedules.dto.response;

import com.toit.schedulesalarm.dto.response.SchedulesAlarmViewResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSchedulesAlarmViewResponse {

    /** 사용자 ID* */
    private Long userId;

    private List<SchedulesAlarmViewResponse> schedulesResponses;


    public PageSchedulesAlarmViewResponse(Long userId, List<SchedulesAlarmViewResponse> schedulesResponses) {
        this.userId = userId;
        this.schedulesResponses = schedulesResponses;
    }
}
