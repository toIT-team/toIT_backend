package com.toit.schedules.dto;

import com.toit.schedules.Schedules;
import lombok.Getter;

import java.time.LocalTime;

@Getter

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

    /**
     * 엔티티(Schedules)를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static SchedulesTodayDto from(Schedules schedule) {
        return new SchedulesTodayDto(
                schedule.getSchedulesId(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getAppColor()
        );

    }
}