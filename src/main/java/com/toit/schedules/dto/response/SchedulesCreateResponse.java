package com.toit.schedules.dto.response;

import com.toit.schedules.Schedules;
import lombok.Getter;

@Getter
public class SchedulesCreateResponse {

    private Long schedulesId;
    private String title;
    private String location;

    public static SchedulesCreateResponse from(Schedules schedule) {
        return new SchedulesCreateResponse(
                schedule.getSchedulesId(),
                schedule.getTitle(),
                schedule.getLocation()
        );
    }

    public SchedulesCreateResponse(Long schedulesId, String title, String location) {
        this.schedulesId = schedulesId;
        this.title = title;
        this.location = location;
    }
}
