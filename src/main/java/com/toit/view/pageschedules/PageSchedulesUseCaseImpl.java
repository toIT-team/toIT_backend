package com.toit.view.pageschedules;


import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.schedules.dto.response.SchedulesMonthResponse;
import com.toit.schedules.dto.response.SchedulesSelectedDayResponse;
import com.toit.notification.alarm.SchedulesAlarm;
import com.toit.notification.alarm.SchedulesAlarmService;
import com.toit.notification.alarm.dto.response.SchedulesAlarmViewResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesAlarmViewResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSearchResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSelectDayViewResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageSchedulesUseCaseImpl implements PageSchedulesUseCase {

    private final SchedulesService schedulesService;
    private final SchedulesAlarmService schedulesAlarmService;

    /***
     * startDate ~ endDate 범위 내의 일정들을 반환
     */
    @Override
    public PageSchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate, LocalDate endDate) {

        List<SchedulesMonthResponse> schedules = schedulesService.getSearchSchedules(usersId, startDate, endDate);

        return new PageSchedulesSearchResponse(schedules);
    }

    /***
     * 선택된 날짜의 스케줄들을 반환
     */
    @Override
    public PageSchedulesSelectDayViewResponse getSelectedDaySchedulesView(Long userId, LocalDate selectedDay){
        List<SchedulesSelectedDayResponse> schedules = schedulesService.getSelectedDaySchedules(userId, selectedDay);

        return new PageSchedulesSelectDayViewResponse(schedules);

    }


    //일정 상세 조회
    @Override
    public ScheduleViewResponse getScheduleView(Long usersId, Long schedulesId){
        ScheduleViewResponse schedule = schedulesService.getSchedule(usersId, schedulesId);

        SchedulesAlarm alarm = schedulesAlarmService.getAlarmBySchedulesId(schedulesId);

        Boolean alarmState = (alarm != null) ? alarm.getAlarmState() : false;
        Long alarmOffsetMinutes = (alarm != null) ? alarm.getAlarmOffsetMinutes() : null;

        return new ScheduleViewResponse(
                schedule.getSchedulesId(),
                schedule.getTitle(),
                schedule.getFoldersId(),
                schedule.getFoldersTitle(),
                schedule.getAppColor(),
                schedule.getTimeSetting(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getMemo(),
                alarmState,
                alarmOffsetMinutes
        );
    }

    //푸시알림 리스트 조회
    @Override
    public PageSchedulesAlarmViewResponse getSchedulesAlarmsView(Long usersId) {

        // 1. 서비스에서 알림 목록(Page) 조회하기
        List<SchedulesAlarm> alarmPage = schedulesAlarmService.getAlarmList(usersId);

        // 2. Stream과 new 생성자를 조합해서 DTO로 변환
        List<SchedulesAlarmViewResponse> schedulesAlarms = alarmPage.stream()
                .map(alarm -> new SchedulesAlarmViewResponse(
                        alarm.getSchedulesAlarmId(),
                        alarm.getSchedules().getTitle(),
                        alarm.getSchedules().getStartDate(),
                        alarm.getSchedules().getStartTime()
                ))
                .collect(Collectors.toList());

        // 3. 만들어두신 최종 껍데기 DTO에 담아서 리턴!
        return new PageSchedulesAlarmViewResponse(schedulesAlarms);
    }

}
