package com.toit.schedules.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesTodayDto {
    private Long schedulesId;
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private String appColor;

    public SchedulesTodayDto(Long schedulesId, String title, LocalTime startTime, LocalTime endTime, String appColor) {
        this.schedulesId = schedulesId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appColor = appColor;
    }


}