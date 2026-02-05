package com.toit.view.schedules.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/***
 * 선택한 날짜의 일정을 조회하는 request
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesSelectDayRequest {
    /** 사용자 ID* */
    private Long usersId ;
    /** 조회할 시작 날짜 ID* */
    private LocalDate selectedDay;
}
