package com.toit.notification.alarm;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface SchedulesAlarmRepository  extends JpaRepository<SchedulesAlarm, Long> {

    Optional<SchedulesAlarm>  findBySchedules_SchedulesId(Long schedulesId);

    /**
     * 보내야 할 알림을 찾아온다.
     *
     * "지금 이 1분에 울릴 것" 이 아니라 "아직 안 보낸 것" 을 기준으로 삼는다.
     * 그래야 서버가 꺼졌던 사이에 지나간 알림도 다음 실행이 회수한다.
     *
     * @param now      스케줄러가 깨어난 시각
     * @param oldest   여기보다 오래된 것은 되살리지 않는다 (유효 시간)
     */
    @Query("SELECT a FROM SchedulesAlarm a " +
            "JOIN FETCH a.schedules s " +
            "JOIN FETCH s.users u " +
            "JOIN UsersSettings us ON us.users = u " +
            "WHERE a.status = com.toit.notification.alarm.AlarmStatus.PENDING " +
            "AND a.alarmDateTime <= :now " +
            "AND a.alarmDateTime >= :oldest " +
            "AND (a.nextAttemptAt IS NULL OR a.nextAttemptAt <= :now) " +
            "AND a.alarmState = true " +
            "AND s.status = 'ACTIVE' " +
            "AND us.appAlarmEnabled = true " +
            "ORDER BY a.alarmDateTime DESC")
    List<SchedulesAlarm> findTargetAlarms(@Param("now") LocalDateTime now,
                                          @Param("oldest") LocalDateTime oldest);


    //알림리스트 조회
    @Query(value = "SELECT a FROM SchedulesAlarm a " +
            "JOIN FETCH a.schedules s " +
            "WHERE s.users.usersId = :usersId " +
            "AND a.status = com.toit.notification.alarm.AlarmStatus.SENT " +
            "ORDER BY a.alarmDateTime DESC" //알림시간을 기준으로 정렬
        )
    List<SchedulesAlarm> findSentAlarmsByUsersId(@Param("usersId") Long usersId);

    /**
     * 회원 탈퇴용 - schedules_id FK 때문에 일정보다 먼저 삭제해야 한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SchedulesAlarm a "
            + "where a.schedules in (select s from Schedules s where s.users.usersId = :usersId)")
    void deleteAllByUsersId(@Param("usersId") Long usersId);

}
