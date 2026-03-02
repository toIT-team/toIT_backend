package com.toit.schedulesalarm.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/***
 * 푸시알림 리스트 response
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesAlarmViewResponse {
    Long alarmId;
    String title;
    LocalDate startDate;
    LocalTime startTime;

    public SchedulesAlarmViewResponse(Long alarmId, String title, LocalDate startDate, LocalTime startTime) {
        this.alarmId = alarmId;
        this.title = title;
        this.startDate = startDate;
        this.startTime = startTime;
    }
}
