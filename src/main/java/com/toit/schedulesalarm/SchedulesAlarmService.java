package com.toit.schedulesalarm;


import com.toit.exception.schedules.SchedulesNotFoundException;
import com.toit.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class SchedulesAlarmService {

    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final UsersService usersService;

    /***
     *스케줄링이 찾을 알림들
     */
    public SchedulesAlarm findBySchedulesAlarm(Long schedulesAlarmId){
        return schedulesAlarmRepository.findById(schedulesAlarmId).
                orElseThrow(()-> new SchedulesNotFoundException(
                        "schedulesAlarmId 가 " + schedulesAlarmId +"인 해당 사용자를 찾을 수 없습니다."));

    }

    public SchedulesAlarm getAlarmBySchedulesId(Long schedulesId) {
        return schedulesAlarmRepository.findBySchedules_SchedulesId(schedulesId)
                .orElse(null);
    }



    /***
     * 푸시 알림 조회
     */
    public List<SchedulesAlarm> getAlarmList(Long usersId) {
        usersService.findById(usersId); // 유저 조회 (예외 case 고려)

        return schedulesAlarmRepository.findSentAlarmsByUsersId(usersId);
    }











}
