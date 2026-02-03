package com.toit.schedules.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesDeleteRequest {

    /** * 수정할 스케줄의 ID*/
    private Long userId;

    /** * 수정할 스케줄의 ID*/
    private Long schedulesId;

}
