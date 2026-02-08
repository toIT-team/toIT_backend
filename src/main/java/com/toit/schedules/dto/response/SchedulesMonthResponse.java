package com.toit.schedules.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesMonthResponse {

    /** 스케줄 ID */
    private Long schedulesId;

    /** 스케줄 제목 */
    private String title;

    /** 시작 날짜  */
    private LocalDate startDate;

    /** 종료 날짜 */
    private LocalDate endDate;

    /** 위젯 색상 */
    private String appColor;

    public SchedulesMonthResponse(Long schedulesId, String title, LocalDate startDate, LocalDate endDate, String appColor) {
        this.schedulesId = schedulesId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appColor = appColor;
    }
}
