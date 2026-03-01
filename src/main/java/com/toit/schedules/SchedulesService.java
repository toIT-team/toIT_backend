package com.toit.schedules;



import com.toit.common.enums.EntityStatus;
import com.toit.exception.schedules.SchedulesNotFoundException;
import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
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
    private final FoldersRepository foldersRepository;
    private final SchedulesAlarmRepository schedulesAlarmRepository;


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
                        selectedDay);

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
                endDate
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
    public SchedulesCreateResponse createSchedule(SchedulesCreateRequest request) {

        Users user = usersService.findById(request.getUsersId());

        Folders folder = null;
        if (request.getFoldersId() != null) {
            folder = foldersRepository.findById(request.getFoldersId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
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
    // 수정이 일어나므로 readOnly = false (기본값)
    public SchedulesUpdateResponse updateSchedules(SchedulesUpdateRequest request) {
        usersService.findById(request.getUsersId());
        Schedules schedule = findBySchedules(request.getSchedulesId());

        Folders folder = null;
        if (request.getFoldersId() != null) {
            folder = foldersRepository.findById(request.getFoldersId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없습니다. ID=" + request.getFoldersId()));
        }

        // 1. 일정(Schedules) 데이터 수정 (Dirty Checking)
        schedule.update(
                request.getTitle(), request.getAppColor(), folder,
                request.getTimeSetting(), request.getStartDate(), request.getEndDate(),
                request.getStartTime(), request.getEndTime(), request.getMemo()
        );

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
     *
     * @param request
     */
    public SchedulesDeleteResponse deleteSchedule(SchedulesDeleteRequest request) {
        usersService.findById(request.getUserId());

        // 1. 스케줄 조회 (없으면 예외 발생)
        Schedules schedule = findBySchedules(request.getSchedulesId());

        usersService.findById(request.getUserId());

        EntityStatus entityStatus = schedule.changeStatusDelete();

        return new SchedulesDeleteResponse(request.getSchedulesId() , request.getUserId(),entityStatus );
    }
}
