package com.toit.schedules.dto.response;


import com.toit.schedules.dto.SchedulesTodayDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesTodayResponse {
    /** 사용자 ID */
    private  Long userId;
    /** 필요한 스케줄의 값들을 리스트로 반환  */
    private  List<SchedulesTodayDto> schedules;

    public SchedulesTodayResponse(Long userId, List<SchedulesTodayDto> schedules) {
        this.userId = userId;
        this.schedules = schedules;
    }

    public static SchedulesTodayResponse of(Long userId, List<SchedulesTodayDto> schedules) {
        return new SchedulesTodayResponse(userId, schedules);
    }
}
