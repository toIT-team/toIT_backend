package com.toit.notification.alarm;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;


@Repository
public interface SchedulesAlarmRepository  extends JpaRepository<SchedulesAlarm, Long> {

    Optional<SchedulesAlarm>  findBySchedules_SchedulesId(Long schedulesId);

    //보내야 할 알림 찾아옴
    @Query("SELECT a FROM SchedulesAlarm a " +
            "JOIN FETCH a.schedules s " +
            "JOIN FETCH s.users u " +
            "JOIN UsersSettings us ON us.users = u " +
            "WHERE a.alarmDateTime >= :start AND a.alarmDateTime < :end " +
            "AND a.alarmState = true " +
            "AND a.isSent = false " +
            "AND s.status = 'ACTIVE' " +
            "AND us.appAlarmEnabled = true")
    List<SchedulesAlarm> findTargetAlarms(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    //알림리스트 조회
    @Query(value = "SELECT a FROM SchedulesAlarm a " +
            "JOIN FETCH a.schedules s " +
            "WHERE s.users.usersId = :usersId " +
            "AND a.isSent = true " +
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
