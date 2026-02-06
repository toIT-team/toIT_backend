package com.toit.schedules.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/***
 * 선택한 일정의 상세보기
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleViewRequest {
    private Long usersId;
    private Long schedulesId;
}
