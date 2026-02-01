package com.toit.schedules.dto.response;

import com.toit.schedules.Schedules;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesCreateResponse {

    /** 스케줄 id*/
    private Long schedulesId;

    /** 스케줄 이름 */
    private String title;


    public SchedulesCreateResponse(Long schedulesId, String title) {
        this.schedulesId = schedulesId;
        this.title = title;

    }
}
