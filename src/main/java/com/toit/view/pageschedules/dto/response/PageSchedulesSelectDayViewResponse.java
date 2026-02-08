package com.toit.view.pageschedules.dto.response;



import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSchedulesSelectDayViewResponse {

    /** 사용자 ID* */
    private Long userId;

    private List<SchedulesSelectedDayResponse> schedulesResponses;

    public PageSchedulesSelectDayViewResponse(Long userId, List<SchedulesSelectedDayResponse> schedulesResponses) {
        this.userId = userId;
        this.schedulesResponses = schedulesResponses;
    }
}
