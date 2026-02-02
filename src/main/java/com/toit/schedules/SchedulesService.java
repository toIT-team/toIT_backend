package com.toit.schedules;



import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
import com.toit.schedules.dto.SchedulesMonthDto;
import com.toit.schedules.dto.SchedulesTodayDto;
import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedules.dto.request.SchedulesMonthRequest;
import com.toit.schedules.dto.request.SchedulesTodayRequest;
import com.toit.schedules.dto.response.SchedulesCreateResponse;
import com.toit.schedules.dto.response.SchedulesMonthResponse;
import com.toit.schedules.dto.response.SchedulesTodayResponse;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchedulesService {

    private final SchedulesRepository schedulesRepository;
    private final UsersRepository usersRepository;
    private final FoldersRepository foldersRepository;

    /***
     * 조회
     */

    /** 오늘 일정 조회 */
    public SchedulesTodayResponse getTodaySchedules(SchedulesTodayRequest request) {
        // startDate <= todayDate AND endDate >= todayDate 조건으로 조회
        List<Schedules> schedules = schedulesRepository
                .findTodaySchedules(request.getUsersId(),
                        request.getTodayDate());

        // 1. 유저 조회
        Users user = usersRepository.findById(request.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<SchedulesTodayDto> scheduleDto = schedules.stream()
                .map(s -> new SchedulesTodayDto(
                         s.getSchedulesId()
                        ,s.getTitle()
                        ,s.getStartTime()
                        ,s.getEndTime()
                        ,s.getAppColor()))
                .collect(Collectors.toList());
        return new SchedulesTodayResponse(request.getUsersId(), scheduleDto);
    }

    /** 시작날짜 ~ 종료날짜 사이 일정 조회 */
    public SchedulesMonthResponse getSchedulesBetween(SchedulesMonthRequest request) {
        // 1. DB 조회 (기간 내 겹치는 모든 일정 가져오기)
        List<Schedules> schedules = schedulesRepository.findSchedulesBetween(
                request.getUsersId(),
                request.getStartDate(),
                request.getEndDate()
        );

        // 2. Entity -> DTO 변환 (SchedulesMonthDto가 있다고 가정)
        List<SchedulesMonthDto> scheduleDtos = schedules.stream()
                .map(s -> new SchedulesMonthDto(
                        s.getSchedulesId(),
                        s.getTitle(),
                        s.getStartDate(),
                        s.getEndDate(),
                        s.getAppColor()
                        // 필요한 필드 추가
                ))
                .collect(Collectors.toList());

        // 3. 응답 반환
        return new SchedulesMonthResponse(request.getUsersId(), scheduleDtos);
    }

    /***
     * 생성
     */

    /** 일정 생성 */
    @Transactional
    public SchedulesCreateResponse createSchedule(SchedulesCreateRequest request) {
        // 1. 유저 조회
        Users user = usersRepository.findById(request.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

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


}
