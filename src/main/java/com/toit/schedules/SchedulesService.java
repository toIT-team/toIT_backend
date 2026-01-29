package com.toit.schedules;



import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedules.dto.response.SchedulesCreateResponse;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchedulesService {

    private final SchedulesRepository schedulesRepository;
    private final UsersRepository usersRepository;
    private final FoldersRepository foldersRepository;

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
        return SchedulesCreateResponse.from(savedSchedule);
    }
}
