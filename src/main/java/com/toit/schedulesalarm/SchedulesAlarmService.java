package com.toit.schedulesalarm;


import com.toit.exception.schedules.SchedulesNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SchedulesAlarmService {
    private SchedulesAlarmRepository schedulesAlarmRepository;

    public SchedulesAlarm findBySchedulesAlarm(Long schedulesAlarmId){
        return schedulesAlarmRepository.findById(schedulesAlarmId).
                orElseThrow(()-> new SchedulesNotFoundException(
                        "schedulesAlarmId 가 " + schedulesAlarmId +"인 해당 사용자를 찾을 수 없습니다."));

    }


}
