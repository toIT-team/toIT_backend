package com.toit.schedulesalarm.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/***
 * 푸시알림 리스트 request
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesAlarmViewRequest {
    private Long usersId;

}
