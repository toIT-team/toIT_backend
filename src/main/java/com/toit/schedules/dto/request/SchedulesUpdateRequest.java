package com.toit.schedules.dto.request;

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
public class SchedulesUpdateRequest {

    /** * 수정할 스케줄의 ID*/
    private Long usersId;

    /** * 수정할 스케줄의 ID*/
    private Long schedulesId;

    /** 제목 */
    private String title;

    /** 앱 컬러 */
    private String appColor;

    /** * 폴더 ID
     * (Entity인 Folders 객체가 아니라, ID값인 Long으로 받아야 합니다)
     */
    private Long foldersId;

    /** 시간 설정 여부 */
    private Boolean timeSetting;

    /** 시작 날짜 */

    private LocalDate startDate;

    /** 종료 날짜 */
    private LocalDate endDate;

    /** 시작 시간  */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(type = "string", example = "09:00:00")
    private LocalTime startTime;

    /** 종료 시간  */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(type = "string", example = "09:00:00")
    private LocalTime endTime;

    /** 메모 */
    private String memo;

    /** 알림 설정 여부 (true: 켬, false: 끔) */
    private Boolean alarmState;

    /** 몇 분 전 알림인지 (예: 5, 10, 0) - alarmState가 true일 때만 값 존재 */
    private Long alarmOffsetMinutes;



    public SchedulesUpdateRequest(Long usersId, Long schedulesId, String title, String appColor,
                                  Long foldersId, Boolean timeSetting, LocalDate startDate,
                                  LocalDate endDate, LocalTime startTime, LocalTime endTime,
                                  String memo,Boolean alarmState , Long alarmOffsetMinutes) {
        this.usersId = usersId;
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