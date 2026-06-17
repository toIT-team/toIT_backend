package com.toit.view.pageschedules;


import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesAlarmViewResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSearchResponse;
import com.toit.view.pageschedules.dto.response.PageSchedulesSelectDayViewResponse;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;

public interface PageSchedulesUseCase {

    /***
     *시작날짜~끝날짜 사이의 일정 조회
     */
    PageSchedulesSearchResponse getSearchSchedulesView(Long usersId, LocalDate startDate, LocalDate endDate);

    /***
     *선택한 날짜의 일정리스트 조회
     */
    PageSchedulesSelectDayViewResponse getSelectedDaySchedulesView(Long usersId, LocalDate selectedDay);

    /***
     *일정 상세 조회
     */
    ScheduleViewResponse getScheduleView(Long usersId, Long schedulesId);

    // [FCM 비활성화] 푸시 알림 조회 비활성화
    // /***
    //  * 푸시 알림 조회
    //  */
    // PageSchedulesAlarmViewResponse getSchedulesAlarmsView(Long userId);




}
