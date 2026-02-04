package com.toit.view.schedules.dto.response;

import com.toit.schedules.dto.response.SchedulesMonthResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesSearchResponse {
    /** 사용자 ID* */
    private Long userId;

    /** 조회된 스케줄 리스트 **/
    private List<SchedulesMonthResponse> schedulesResponses;

    public SchedulesSearchResponse(Long userId, List<SchedulesMonthResponse> schedulesResponses) {
        this.userId = userId;
        this.schedulesResponses = schedulesResponses;
    }
}
