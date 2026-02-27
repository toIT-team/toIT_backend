package com.toit.schedules.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesUpdateResponse {
    /** 스케줄 ID */
    private Long schedulesId;
    /** 스케줄 제목 ID */
    private String title;
    /** 스케줄 위젯 색상 */
    private String appColor;
    /** 스케줄과 mapping 된 폴더 */
    private Long foldersId;
    /** 스케줄 시간 설정 */
    private Boolean timeSetting;
    /** 시작 날짜 */
    private LocalDate startDate;
    /** 종료 날짜 */
    private LocalDate endDate;
    /** 시작 시간 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(type = "string", example = "09:00:00")
    private LocalTime startTime;
    /** 종료 시간 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(type = "string", example = "09:00:00")
    private LocalTime endTime;
    /** 알림 설정 */

    /** 스케줄의 메모  */
    private String memo;
    /***
     * 알림 설정 여부 (켰는지 , 안 켰는지)
     */
    private Boolean alarmState;

    /***
     * 정수타입의 몇분전에 알림을 설정 했는지 (5분,10분 , 직접 설정)
     */
    private Long alarmOffsetMinutes;



    public SchedulesUpdateResponse(Long schedulesId, String title, String appColor, Long foldersId,
                                   Boolean timeSetting, LocalDate startDate, LocalDate endDate,
                                   LocalTime startTime, LocalTime endTime,
                                  String memo, Boolean alarmState,Long alarmOffsetMinutes) {
        this.schedulesId = schedulesId;
        this.title = title;
        this.appColor = appColor;
        this.foldersId = foldersId;
        this.timeSetting = timeSetting;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.alarmState = alarmState;
        this.alarmOffsetMinutes = alarmOffsetMinutes;
    }
}
