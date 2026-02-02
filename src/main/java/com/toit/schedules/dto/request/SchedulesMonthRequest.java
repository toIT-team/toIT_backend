package com.toit.schedules.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesMonthRequest {
    /** 사용자 ID */
    private Long usersId;
    /** 오늘 날짜  */
    private LocalDate startDate;
    /** 종료 날짜  */
    private LocalDate endDate;
}
