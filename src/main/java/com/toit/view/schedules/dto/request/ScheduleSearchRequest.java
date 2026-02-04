package com.toit.view.schedules.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/***
 * startDate ~ endDate 사이의 범위 조회를 위한 request
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleSearchRequest {
    /** 사용자 ID* */
    private Long userId ;
    /** 조회할 시작 날짜 ID* */
    private LocalDate startDate;
    /** 조회할 종료 날짜 **/
    private LocalDate endDate;

    public ScheduleSearchRequest(Long userId, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
