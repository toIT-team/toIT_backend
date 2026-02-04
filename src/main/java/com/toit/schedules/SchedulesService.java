package com.toit.schedules;



import com.toit.common.enums.EntityStatus;
import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
import com.toit.schedules.dto.request.SchedulesDeleteRequest;
import com.toit.schedules.dto.request.SchedulesUpdateRequest;
import com.toit.schedules.dto.response.*;
import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SchedulesService {

    private final SchedulesRepository schedulesRepository;
    private final UsersService usersService;
    private final FoldersRepository foldersRepository;

    public Schedules findBySchedules(Long schedulesId){
        Optional<Schedules> schedules = schedulesRepository.findById(schedulesId);

        if (schedules.isPresent()) {
            return schedules.get();
        } else {
            throw new IllegalArgumentException("존재하지 않는 일정입니다. usersId=" + schedulesId);
        }
    }

    /**
     * 오늘 일정 조회
     * @param usersId
     * @param todayDate
     * @return
     */
    public List<SchedulesTodayResponse> getTodaySchedules(Long usersId, LocalDate todayDate) {
        // startDate <= todayDate AND endDate >= todayDate 조건으로 조회
        List<Schedules> schedules = schedulesRepository
                .findTodaySchedules(usersId,
                        todayDate);

        // 유저 조회
        usersService.findById(usersId);

        List<SchedulesTodayResponse> scheduleDto = schedules.stream()
                .map(s -> new SchedulesTodayResponse(
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
     *
     * @param request
     * 스케줄 생성
     * @return
     */
    public SchedulesCreateResponse createSchedule(SchedulesCreateRequest request) {
        // 1. 유저 조회
        Users user = usersService.findById(request.getUsersId());
        // 2. 폴더 조회 (null 허용)
        Folders folder = null;
        if (request.getFoldersId() != null) {
            folder = foldersRepository.findById(request.getFoldersId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        }

        // 3. 팩토리 메서드로 엔티티 생성
        // 3. 생성자를 호출하여 엔티티 생성
        // DTO의 LocalDate/LocalTime을 그대로 전달합니다.
        Schedules schedule = new Schedules(
                request.getTitle(),
                request.getAppColor(),
                folder,
                request.getTimeSetting(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation(),
                request.getNotification(),
                request.getMemo(),
                user
        );


        // 3. 저장 및 응답 DTO 변환 반환
        Schedules savedSchedule = schedulesRepository.save(schedule);
        return new  SchedulesCreateResponse(savedSchedule.getSchedulesId(), savedSchedule.getTitle());
    }


    /***
     * 수정 영역
     */


    /***
     * 스케줄 수정 로직
     * @param request
     */
    @Transactional // 수정이 일어나므로 readOnly = false (기본값)
    public SchedulesUpdateResponse updateSchedules(SchedulesUpdateRequest request) {

        // 1. 스케줄 조회 (없으면 예외 발생)
        Schedules schedule = findBySchedules(request.getSchedulesId());


        // 2. 폴더 조회 (폴더 ID가 들어온 경우에만)
        Folders folder = null;
        if (request.getFoldersId() != null) {
            folder = foldersRepository.findById(request.getFoldersId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없습니다. ID=" + request.getFoldersId()));
        }

        // 4. 데이터 수정 (Dirty Checking - save 호출 불필요)
        schedule.update(
                request.getTitle(),
                request.getAppColor(),
                folder,
                request.getTimeSetting(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation(),
                request.getNotification(),
                request.getMemo()
        );

        // 5. 변경된 엔티티를 Response DTO로 변환하여 반환
        return new SchedulesUpdateResponse(
                schedule.getSchedulesId(),
                schedule.getTitle(),
                schedule.getAppColor(),
                schedule.getFolders() != null ? schedule.getFolders().getFoldersId() : null, // 폴더가 없을 경우 null 처리
                schedule.getTimeSetting(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getLocation(),
                schedule.getNotification(),
                schedule.getMemo()
        );
    }

    /***
     *
     * @param request
     */
    public SchedulesDeleteResponse deleteSchedule(SchedulesDeleteRequest request) {

        // 1. 스케줄 조회 (없으면 예외 발생)
        Schedules schedule = findBySchedules(request.getSchedulesId());

        usersService.findById(request.getUserId());

        EntityStatus entityStatus = schedule.changeStatusDelete();

        return new SchedulesDeleteResponse(request.getSchedulesId() , request.getUserId(),entityStatus );
    }
}
