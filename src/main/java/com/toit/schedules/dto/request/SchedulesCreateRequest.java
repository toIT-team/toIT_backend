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
public class SchedulesCreateRequest {

    /** 스케줄 제목 */
    private String title;

    /** 컬러 ENUM 값 */
    private String appColor;

    /** 선택한 폴더의 ID (어느 폴더에 저장할지) */
    private Long foldersId;

    /** 시간 설정 여부 */
    private Boolean timeSetting;

    /** 시작 날짜  */

    private LocalDate startDate;

    /** 시작 시간  */
    @Schema(type = "string", example = "09:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;

    /** 종료 날짜  */
    private LocalDate endDate;

    /** 종료 시간 */
    @Schema(type = "string", example = "09:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;

    /** 입력한 메모  */
    private String memo;

    // [FCM 비활성화] 일정 알림 설정 필드 중단
    // /** 알림 설정 여부 (true: 켬, false: 끔) */
    // private Boolean alarmState;
    //
    // /** 몇 분 전 알림인지 (예: 5, 10, 0) - alarmState가 true일 때만 값 존재 */
    // private Long alarmOffsetMinutes;


}
