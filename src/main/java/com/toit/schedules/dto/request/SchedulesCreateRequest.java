package com.toit.schedules.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesCreateRequest {

    /** 사용자 ID */
    private Long usersId;

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
    private LocalTime startTime;

    /** 종료 날짜  */
    private LocalDate endDate;

    /** 종료 시간 */
    private LocalTime endTime;

    /** 설정한 위치  */
    private String location;

    /** 알림 설정 */
    private Boolean notification;

    /** 입력한 메모  */
    private String memo;
}
