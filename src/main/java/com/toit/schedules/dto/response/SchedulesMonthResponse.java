package com.toit.schedules.dto.response;

import com.toit.schedules.dto.SchedulesMonthDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesMonthResponse {

    /** 사용자 ID */
    private  Long userId;

    /** 필요한 스케줄의 값들을 리스트로 반환  */
    private List<SchedulesMonthDto> schedules;

    public SchedulesMonthResponse(Long userId, List<SchedulesMonthDto> schedules) {
        this.userId = userId;
        this.schedules = schedules;
    }
}
