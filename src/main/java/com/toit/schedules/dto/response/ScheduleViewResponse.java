package com.toit.schedules.dto.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleViewResponse {

    /** 사용자 ID */
    private Long userId;

    /** 스케줄 ID */
    private Long schedulesId;

    /** 스케줄 제목 ID */
    private String title;

    /** 스케줄과 mapping 된 폴더Id */
    private Long foldersId;

    /** 스케줄과 mapping 된 폴더 제목 */
    private String foldersTitle;

    /** 스케줄 시간 설정 */
    private Boolean timeSetting;

    /** 시작 날짜 */
    private LocalDate startDate;

    /** 종료 날짜 */
    private LocalDate endDate;

    /** 시작 시간 */
    private LocalTime startTime;

    /** 종료 시간 */
    private LocalTime endTime;

    /** 위치 */
    private String location;

    /** 알림 설정 */
    private Boolean notification;

    /** 스케줄의 메모  */
    private String memo;

    public ScheduleViewResponse(Long userId, Long schedulesId,
                                String title, Long foldersId,
                                String foldersTitle,
                                Boolean timeSetting, LocalDate startDate,
                                LocalDate endDate, LocalTime startTime,
                                LocalTime endTime, String location,
                                Boolean notification, String memo) {
        this.userId = userId;
        this.schedulesId = schedulesId;
        this.title = title;
        this.foldersId = foldersId;
        this.foldersTitle  = foldersTitle;
        this.timeSetting = timeSetting;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.notification = notification;
        this.memo = memo;
    }
}
