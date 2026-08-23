package com.toit.schedules;



import com.toit.common.enums.EntityStatus;
import com.toit.exception.schedules.ScheduleTimeRangeException;
import com.toit.exception.schedules.SchedulesNotFoundException;
import com.toit.folders.Folders;
import com.toit.folders.FoldersService;
import com.toit.schedules.dto.request.SchedulesDeleteRequest;
import com.toit.schedules.dto.request.SchedulesUpdateRequest;
import com.toit.schedules.dto.response.*;
import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedulesalarm.SchedulesAlarm;
import com.toit.schedulesalarm.SchedulesAlarmRepository;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulesService {

    private final SchedulesRepository schedulesRepository;
    private final UsersService usersService;
    private final FoldersService foldersService;
    private final SchedulesAlarmRepository schedulesAlarmRepository;

    /**
     * 일정 ID로 일정을 조회하고, 없으면 예외를 발생시킨다.
     */
    public Schedules findBySchedules(Long schedulesId){
        return schedulesRepository.findById(schedulesId).
                orElseThrow(()-> new SchedulesNotFoundException(
                        "schedulesId 가 " + schedulesId +"인 해당 사용자를 찾을 수 없습니다."));

    }

    /**
     * 일정 상세를 조회
     */
    public ScheduleViewResponse getSchedule(Long usersId,Long schedulesId){

        usersService.findById(usersId);

        Schedules schedules = schedulesRepository
                .findBySchedulesIdAndUsers_UsersIdAndStatus(schedulesId, usersId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new SchedulesNotFoundException(
                        "schedulesId 가 " + schedulesId + "인 일정을 찾을 수 없습니다."));

        Folders folders = schedules.getFolders();

        SchedulesAlarm alarm = schedulesAlarmRepository.findBySchedules_SchedulesId(schedulesId)
                .orElse(null);

        markAlarmAsReadIfNeeded(alarm);

        Boolean alarmState = (alarm != null) ? alarm.getAlarmState() : false;
        Long alarmOffsetMinutes = (alarm != null) ? alarm.getAlarmOffsetMinutes() : null;

        return new ScheduleViewResponse(
                schedules.getSchedulesId(),
                schedules.getTitle(),
                (folders != null) ? folders.getFoldersId() : null,
                (folders != null) ? folders.getName() : null,
                schedules.getAppColor(),
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

    /**
     * 이미 발송된 일정 알림이 아직 미읽음 상태일 때만 읽음 처리한다.
     */
    private void markAlarmAsReadIfNeeded(SchedulesAlarm alarm) {
        if (alarm == null) {
            return;
        }
        if (!Boolean.TRUE.equals(alarm.getIsSent()) || Boolean.TRUE.equals(alarm.getIsRead())) {
            return;
        }
        alarm.markAsRead();
        schedulesAlarmRepository.save(alarm);
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
                        ,s.getStartDate()
                        ,s.getEndDate()
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
        validateScheduleTimeRange(
                request.getTimeSetting(),
                request.getStartDate(),
                request.getStartTime(),
                request.getEndDate(),
                request.getEndTime()
        );

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

        // 클라이언트가 알림 값을 실제로 보냈는지 확인하려고 받은 그대로 남긴다.
        // alarmState=null 이면 필드를 아예 안 보낸 것이다.
        log.info("[ALARM] 생성요청 usersId={} scheduleId={} alarmState={} offset={} timeSetting={} 시작={} {}",
                usersId, savedSchedule.getSchedulesId(),
                request.getAlarmState(), request.getAlarmOffsetMinutes(),
                request.getTimeSetting(), request.getStartDate(), request.getStartTime());

        // 알림 설정이 켜져 있는 경우만 실행
        if (Boolean.TRUE.equals(request.getAlarmState())) {

            LocalDateTime alarmDateTime;

            if (request.getTimeSetting() && request.getStartTime() != null) {
                // 1. 일정 시간 설정이 켜져 있는 경우: (시작 시간 - N분 전)
                LocalDateTime baseDateTime = LocalDateTime.of(request.getStartDate(), request.getStartTime());

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

            log.info("[ALARM] 예약생성 alarmId={} usersId={} scheduleId={} 예정={} offset={}분",
                    alarm.getSchedulesAlarmId(), usersId, savedSchedule.getSchedulesId(),
                    alarmDateTime, request.getAlarmOffsetMinutes());
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
        validateScheduleTimeRange(
                request.getTimeSetting(),
                request.getStartDate(),
                request.getStartTime(),
                request.getEndDate(),
                request.getEndTime()
        );
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

        // 클라이언트가 알림 값을 실제로 보냈는지 확인하려고 받은 그대로 남긴다.
        // alarmState=null 이면 필드를 아예 안 보낸 것이다.
        log.info("[ALARM] 수정요청 usersId={} scheduleId={} alarmState={} offset={} timeSetting={} 시작={} {}",
                usersId, schedule.getSchedulesId(),
                request.getAlarmState(), request.getAlarmOffsetMinutes(),
                request.getTimeSetting(), request.getStartDate(), request.getStartTime());

        // 기존에 설정된 알림이 있는지 조회
        Optional<SchedulesAlarm> existingAlarm = schedulesAlarmRepository.findBySchedules_SchedulesId(schedule.getSchedulesId());

        if (Boolean.TRUE.equals(request.getAlarmState())) {
            // 알림을 켜는(또는 유지하는) 경우: 알림 시간 재계산
            LocalDateTime alarmDateTime;
            if (request.getTimeSetting() && request.getStartTime() != null) {
                long offset = (request.getAlarmOffsetMinutes() != null) ? request.getAlarmOffsetMinutes() : 0L;
                alarmDateTime = LocalDateTime.of(request.getStartDate(), request.getStartTime()).minusMinutes(offset);
            } else {
                alarmDateTime = LocalDateTime.of(request.getStartDate(), LocalTime.of(9, 0));
            }

            if (existingAlarm.isPresent()) {
                SchedulesAlarm alarm = existingAlarm.get();
                //  기존 알림이 있음 -> 정보 업데이트 및 상태(isSent, isRead) 초기화
                alarm.updateAlarm(request.getAlarmState(), alarmDateTime, request.getAlarmOffsetMinutes());
                //알림 변경 내용 저장
                schedulesAlarmRepository.save(alarm);

                log.info("[ALARM] 예약갱신 alarmId={} usersId={} scheduleId={} 예정={} offset={}분",
                        alarm.getSchedulesAlarmId(), usersId, schedule.getSchedulesId(),
                        alarmDateTime, request.getAlarmOffsetMinutes());
            } else {
                //  기존 알림이 없음 (새로 켬) -> 새로 생성
                SchedulesAlarm newAlarm = new SchedulesAlarm(schedule, request.getAlarmState(), alarmDateTime, request.getAlarmOffsetMinutes());
                schedulesAlarmRepository.save(newAlarm);

                log.info("[ALARM] 예약생성 alarmId={} usersId={} scheduleId={} 예정={} offset={}분",
                        newAlarm.getSchedulesAlarmId(), usersId, schedule.getSchedulesId(),
                        alarmDateTime, request.getAlarmOffsetMinutes());
            }
        } else {
            // 알림을 끄는 경우
            existingAlarm.ifPresent(alarm -> {
                schedulesAlarmRepository.delete(alarm);
                log.info("[ALARM] 예약삭제 alarmId={} usersId={} scheduleId={}",
                        alarm.getSchedulesAlarmId(), usersId, schedule.getSchedulesId());
            });
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
                responseAlarmState, responseAlarmOffset
        );
    }

    /**
     * 종료 날짜가 시작 날짜보다 빠른 경우를 막고,
     * 같은 날에 시간 설정이 켜진 일정만 종료 시각이 시작 시각보다 빠른 경우를 막는다.
     */
    private void validateScheduleTimeRange(Boolean timeSetting,
                                           LocalDate startDate,
                                           LocalTime startTime,
                                           LocalDate endDate,
                                           LocalTime endTime) {
        if (startDate == null || endDate == null) {
            return;
        }

        if (endDate.isBefore(startDate)) {
            throw new ScheduleTimeRangeException("종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
        }

        if (!Boolean.TRUE.equals(timeSetting)) {
            return;
        }
        if (startTime == null || endTime == null) {
            return;
        }

        if (startDate.isEqual(endDate) && endTime.isBefore(startTime)) {
            throw new ScheduleTimeRangeException("종료 시간은 시작 시간보다 빠를 수 없습니다.");
        }
    }


    /***
     * 일정 삭제
     */
    public SchedulesDeleteResponse deleteSchedule(Long usersId, SchedulesDeleteRequest request) {
        usersService.findById(usersId);

        Schedules schedule = findBySchedules(request.getSchedulesId());

        schedule.changeStatusDelete();

        schedulesRepository.save(schedule);

        return new SchedulesDeleteResponse(request.getSchedulesId(), schedule.getStatus());
    }


    /**
     * 제목 키워드로 사용자의 일정을 검색한다.
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
            Folders folders = schedule.getFolders();

            responses.add(new ScheduleViewResponse(
                    schedule.getSchedulesId(),
                    schedule.getTitle(),
                    (folders != null) ? folders.getFoldersId() : null,
                    (folders != null) ? folders.getName() : null,
                    schedule.getAppColor(),
                    schedule.getTimeSetting(),
                    schedule.getStartDate(),
                    schedule.getEndDate(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getMemo(),
                    // 검색 목록은 알람을 조회하지 않는다. 일정마다 조회하면 N+1 이 되고,
                    // 알림 설정 여부는 상세 화면에서만 필요하다.
                    false,
                    null
            ));
        }
        return responses;
    }


}
