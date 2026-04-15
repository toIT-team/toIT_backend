package com.toit.schedules;



import com.toit.common.enums.EntityStatus;
import com.toit.exception.schedules.SchedulesNotFoundException;
import com.toit.folders.Folders;
import com.toit.folders.FoldersService;
import com.toit.schedules.dto.request.SchedulesDeleteRequest;
import com.toit.schedules.dto.request.SchedulesUpdateRequest;
import com.toit.schedules.dto.response.*;
import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedulesalarm.SchedulesAlarm;
import com.toit.schedulesalarm.SchedulesAlarmRepository;
import com.toit.schedulesalarm.SchedulesAlarmService;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulesService {

    private final SchedulesRepository schedulesRepository;
    private final UsersService usersService;
    private final FoldersService foldersService;
    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final SchedulesAlarmService schedulesAlarmService;

    public Schedules findBySchedules(Long schedulesId){
        return schedulesRepository.findById(schedulesId).
                orElseThrow(()-> new SchedulesNotFoundException(
                        "schedulesId 가 " + schedulesId +"인 해당 사용자를 찾을 수 없습니다."));

    }

    //일정 상세조회
    public ScheduleViewResponse getSchedule(Long usersId,Long schedulesId){

        usersService.findById(usersId);

        Schedules schedules = findBySchedules(schedulesId);

        Folders folders = schedules.getFolders();

        SchedulesAlarm alarm = schedulesAlarmRepository.findBySchedules_SchedulesId(schedulesId)
                .orElse(null);

        Boolean alarmState = (alarm != null) ? alarm.getAlarmState() : false;
        Long alarmOffsetMinutes = (alarm != null) ? alarm.getAlarmOffsetMinutes() : null;

        return new ScheduleViewResponse(
                usersId,
                schedules.getSchedulesId(),
                schedules.getTitle(),
                (folders != null) ? folders.getFoldersId() : null, // 폴더가 없으면 ID도 null
                (folders != null) ? folders.getName() : null,      // 폴더가 없으면 제목도 null
                schedules.getTimeSetting(),
                schedules.getStartDate(),
                schedules.getEndDate(),
                schedules.getStartTime(),
                schedules.getEndTime(),
                schedules.getMemo(),
                alarmState,
                alarmOffsetMinutes
        );
    }


    /***
     * 조회 영역
     */

    /**
     * 선택된 날짜 일정 조회
     * @param usersId
     * @param selectedDay
     * @return
     */
    public List<SchedulesSelectedDayResponse> getSelectedDaySchedules(Long usersId, LocalDate selectedDay) {
        // startDate <= todayDate AND endDate >= todayDate 조건으로 조회
        List<Schedules> schedules = schedulesRepository
                .findSelectedDaySchedules(usersId,
                        selectedDay,EntityStatus.ACTIVE);

        // 유저 조회
        usersService.findById(usersId);

        List<SchedulesSelectedDayResponse> scheduleDto = schedules.stream()
                .map(s -> new SchedulesSelectedDayResponse(
                         s.getSchedulesId()
                        ,s.getTitle()
                        ,s.getStartTime()
                        ,s.getEndTime()
                        ,s.getAppColor()))
                .collect(Collectors.toList());

        return scheduleDto;
    }

    /** 시작날짜 ~ 종료날짜 사이 일정 조회 */
    public List<SchedulesMonthResponse> getSearchSchedules(Long usersId,LocalDate startDate, LocalDate endDate) {
        // 1. DB 조회 (기간 내 겹치는 모든 일정 가져오기)
        List<Schedules> schedules = schedulesRepository.findSchedulesBetween(
                usersId,
                startDate,
                endDate,
                EntityStatus.ACTIVE
        );

        // 2. Entity -> DTO 변환 (SchedulesMonthDto가 있다고 가정)
        List<SchedulesMonthResponse> scheduleDtos = schedules.stream()
                .map(s -> new SchedulesMonthResponse(
                        s.getSchedulesId(),
                        s.getTitle(),
                        s.getStartDate(),
                        s.getEndDate(),
                        s.getAppColor()
                        // 필요한 필드 추가
                ))
                .collect(Collectors.toList());

        // 3. 응답 반환
        return scheduleDtos;
    }

    /***
     * 생성 영역 ------
     */
    /***
     *
     * @param request
     * 스케줄 생성
     * @return
     */
    public SchedulesCreateResponse createSchedule(Long usersId, SchedulesCreateRequest request) {

        Users user = usersService.findById(usersId);

        Folders folder = null;
        if (request.getFoldersId() != null) {
            folder = foldersService.findById(request.getFoldersId());
        }


        // 3. 생성자를 호출하여 엔티티 생성
        Schedules schedule = new Schedules(
                request.getTitle(),
                request.getAppColor(),
                folder,
                request.getTimeSetting(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMemo(),
                user
        );
        Schedules savedSchedule = schedulesRepository.save(schedule);

        // 알림 설정이 켜져 있는 경우만 실행
        if (Boolean.TRUE.equals(request.getAlarmState())) {

            LocalDateTime alarmDateTime;

            if (request.getTimeSetting() && request.getEndTime() != null) {
                // 1. 일정 시간 설정이 켜져 있는 경우: (종료 시간 - N분 전)
                LocalDateTime baseDateTime = LocalDateTime.of(request.getEndDate(), request.getEndTime());

                // NullPointerException 방지를 위해 offset이 null이면 0으로 처리 (혹은 기획에 맞게 예외처리)
                long offset = (request.getAlarmOffsetMinutes() != null) ? request.getAlarmOffsetMinutes() : 0L;
                alarmDateTime = baseDateTime.minusMinutes(offset);

            } else {
                // 2. 일정 시간 설정이 꺼져 있는 경우 (종일 일정 등):
                // 시작 날짜(StartDate)의 오전 9시 0분 정각으로 알림 시간 세팅
                alarmDateTime = LocalDateTime.of(request.getStartDate(), LocalTime.of(9, 0));
            }

            // 알림 엔티티 생성 및 저장
            SchedulesAlarm alarm = new SchedulesAlarm(
                    savedSchedule,
                    request.getAlarmState(),
                    alarmDateTime,
                    request.getAlarmOffsetMinutes()
            );
            schedulesAlarmRepository.save(alarm);
        }

        return new  SchedulesCreateResponse(savedSchedule.getSchedulesId(), savedSchedule.getTitle());
    }


    /***
     * 수정 영역
     */


    /***
     * 스케줄 수정 로직
     * @param request
     */

    public SchedulesUpdateResponse updateSchedules(Long usersId, SchedulesUpdateRequest request) {
        usersService.findById(usersId);
        Schedules schedule = findBySchedules(request.getSchedulesId());

        Folders folder = null;
        if (request.getFoldersId() != null) {
            folder = foldersService.findById(request.getFoldersId());
        }

        // 1. 일정(Schedules) 데이터 수정 (Dirty Checking)
        schedule.update(
                request.getTitle(), request.getAppColor(), folder,
                request.getTimeSetting(), request.getStartDate(), request.getEndDate(),
                request.getStartTime(), request.getEndTime(), request.getMemo()
        );
        schedulesRepository.save(schedule);

        // 기존에 설정된 알림이 있는지 조회
        Optional<SchedulesAlarm> existingAlarm = schedulesAlarmRepository.findBySchedules_SchedulesId(schedule.getSchedulesId());

        if (Boolean.TRUE.equals(request.getAlarmState())) {
            // 알림을 켜는(또는 유지하는) 경우: 알림 시간 재계산
            LocalDateTime alarmDateTime;
            if (request.getTimeSetting() && request.getEndTime() != null) {
                long offset = (request.getAlarmOffsetMinutes() != null) ? request.getAlarmOffsetMinutes() : 0L;
                alarmDateTime = LocalDateTime.of(request.getEndDate(), request.getEndTime()).minusMinutes(offset);
            } else {
                alarmDateTime = LocalDateTime.of(request.getStartDate(), LocalTime.of(9, 0));
            }

            if (existingAlarm.isPresent()) {
                SchedulesAlarm alarm = existingAlarm.get();
                //  기존 알림이 있음 -> 정보 업데이트 및 상태(isSent, isRead) 초기화
                alarm.updateAlarm(request.getAlarmState(), alarmDateTime, request.getAlarmOffsetMinutes());
                //알림 변경 내용 저장
                schedulesAlarmRepository.save(alarm);
            } else {
                //  기존 알림이 없음 (새로 켬) -> 새로 생성
                SchedulesAlarm newAlarm = new SchedulesAlarm(schedule, request.getAlarmState(), alarmDateTime, request.getAlarmOffsetMinutes());
                schedulesAlarmRepository.save(newAlarm);
            }
        } else {
            // 알림을 끄는 경우
            existingAlarm.ifPresent(alarm -> schedulesAlarmRepository.delete(alarm));
        }

        // request.getAlarmState()가 null일 수 있으므로 안전하게 boolean으로 변환
        boolean responseAlarmState = Boolean.TRUE.equals(request.getAlarmState());

        // 알림이 꺼져있다면 offset은 무조건 null로 내려주는 것이 프론트엔드 렌더링에 안전합니다.
        Long responseAlarmOffset = responseAlarmState ? request.getAlarmOffsetMinutes() : null;


        return new SchedulesUpdateResponse(
                schedule.getSchedulesId(), schedule.getTitle(), schedule.getAppColor(),
                schedule.getFolders() != null ? schedule.getFolders().getFoldersId() : null,
                schedule.getTimeSetting(), schedule.getStartDate(), schedule.getEndDate(),
                schedule.getStartTime(), schedule.getEndTime(), schedule.getMemo(),
                responseAlarmState,responseAlarmOffset
        );
    }


    /***
     * 일정 삭제
     */
    public SchedulesDeleteResponse deleteSchedule(Long usersId, SchedulesDeleteRequest request) {
        usersService.findById(usersId);

        Schedules schedule = findBySchedules(request.getSchedulesId());

        schedule.changeStatusDelete();

        schedulesRepository.save(schedule);

        return new SchedulesDeleteResponse(request.getSchedulesId(), usersId, schedule.getStatus());
    }


    /**
     * 검색
     */

    public List<ScheduleViewResponse> searchSchedules(Long usersId, String keyword){
        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) {
            return List.of();
        }
        List<Schedules> schedules =
                schedulesRepository.searchByTitle(usersId, EntityStatus.ACTIVE, k);

        List<ScheduleViewResponse> responses = new ArrayList<>();

        for (Schedules schedule : schedules) {
            SchedulesAlarm bySchedulesAlarm = schedulesAlarmService.findBySchedulesAlarm(schedule.getSchedulesId());
            responses.add(new ScheduleViewResponse(
                    usersId,
                    schedule.getSchedulesId(),
                    schedule.getTitle(),
                    schedule.getFolders().getFoldersId(),
                    schedule.getFolders().getName(),
                    schedule.getTimeSetting(),
                    schedule.getStartDate(),
                    schedule.getEndDate(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getMemo(),
                    bySchedulesAlarm.getAlarmState(),
                    bySchedulesAlarm.getAlarmOffsetMinutes()
            ));
        }
        return responses;
    }


}
