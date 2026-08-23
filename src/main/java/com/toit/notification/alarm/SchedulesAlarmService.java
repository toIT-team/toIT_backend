package com.toit.notification.alarm;


import com.toit.schedules.Schedules;
import com.toit.schedules.exception.SchedulesNotFoundException;
import com.toit.user.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Slf4j
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

    /**
     * 이미 발송된 알림이 아직 미읽음 상태일 때만 읽음 처리한다.
     */
    public void markAsReadIfNeeded(SchedulesAlarm alarm) {
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
     * 예약 영역
     */

    /**
     * 일정의 알림 예약을 요청한 상태로 맞춘다.
     * 켜면 새로 만들거나 이미 있던 예약을 갱신하고, 끄면 있던 예약을 지운다.
     *
     * 일정을 새로 만든 직후에는 기존 예약이 있을 수 없어 항상 생성 쪽으로 간다.
     * 생성과 수정이 같은 규칙을 쓰도록 한 메서드로 둔다.
     */
    public void applyAlarm(Long usersId, Schedules schedule, Boolean alarmState,
                           Long alarmOffsetMinutes, Boolean timeSetting,
                           LocalDate startDate, LocalTime startTime) {

        Optional<SchedulesAlarm> existingAlarm =
                schedulesAlarmRepository.findBySchedules_SchedulesId(schedule.getSchedulesId());

        if (!Boolean.TRUE.equals(alarmState)) {
            existingAlarm.ifPresent(alarm -> {
                schedulesAlarmRepository.delete(alarm);
                log.info("[ALARM] 예약삭제 alarmId={} usersId={} scheduleId={}",
                        alarm.getSchedulesAlarmId(), usersId, schedule.getSchedulesId());
            });
            return;
        }

        LocalDateTime alarmDateTime =
                calculateAlarmDateTime(timeSetting, startDate, startTime, alarmOffsetMinutes);

        if (existingAlarm.isPresent()) {
            SchedulesAlarm alarm = existingAlarm.get();
            // 예약을 다시 잡는 것이므로 발송·읽음 상태도 함께 초기화된다.
            alarm.updateAlarm(alarmState, alarmDateTime, alarmOffsetMinutes);
            schedulesAlarmRepository.save(alarm);

            log.info("[ALARM] 예약갱신 alarmId={} usersId={} scheduleId={} 예정={} offset={}분",
                    alarm.getSchedulesAlarmId(), usersId, schedule.getSchedulesId(),
                    alarmDateTime, alarmOffsetMinutes);
            return;
        }

        SchedulesAlarm newAlarm =
                new SchedulesAlarm(schedule, alarmState, alarmDateTime, alarmOffsetMinutes);
        schedulesAlarmRepository.save(newAlarm);

        log.info("[ALARM] 예약생성 alarmId={} usersId={} scheduleId={} 예정={} offset={}분",
                newAlarm.getSchedulesAlarmId(), usersId, schedule.getSchedulesId(),
                alarmDateTime, alarmOffsetMinutes);
    }

    /**
     * 알림을 울릴 시각을 정한다.
     *
     * 시간을 설정한 일정은 시작 시각에서 offset 만큼 앞당기고,
     * 종일 일정은 울릴 시각이 없으므로 시작 날짜 오전 9시로 잡는다.
     */
    private LocalDateTime calculateAlarmDateTime(Boolean timeSetting, LocalDate startDate,
                                                 LocalTime startTime, Long alarmOffsetMinutes) {
        if (Boolean.TRUE.equals(timeSetting) && startTime != null) {
            long offset = (alarmOffsetMinutes != null) ? alarmOffsetMinutes : 0L;
            return LocalDateTime.of(startDate, startTime).minusMinutes(offset);
        }
        return LocalDateTime.of(startDate, LocalTime.of(9, 0));
    }

}
