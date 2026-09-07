package com.toit.notification.alarm;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 보낼 알림의 번호만 가져온다.
     *
     * 엔티티를 스레드에 넘기지 않는다. 2,500건을 12분에 걸쳐 처리하면 마지막 건은
     * 조회한 지 12분 된 상태로 나가는데, 번호만 넘기고 처리 직전에 다시 읽으면
     * 그 틈이 줄어든다. 분리된 엔티티를 저장하면 어차피 merge 로 SELECT 가 한 번
     * 더 나가므로 조회가 늘어나는 것도 아니다.
     * 조건은 findTargetAlarms 와 같다.
     */
    @Query("SELECT a.schedulesAlarmId FROM SchedulesAlarm a " +
            "JOIN a.schedules s " +
            "JOIN s.users u " +
            "JOIN UsersSettings us ON us.users = u " +
            "WHERE a.status = com.toit.notification.alarm.AlarmStatus.PENDING " +
            "AND a.alarmDateTime <= :now " +
            "AND a.alarmDateTime >= :oldest " +
            "AND (a.nextAttemptAt IS NULL OR a.nextAttemptAt <= :now) " +
            "AND a.alarmState = true " +
            "AND s.status = 'ACTIVE' " +
            "AND us.appAlarmEnabled = true " +
            "ORDER BY a.alarmDateTime DESC")
    List<Long> findTargetAlarmIds(@Param("now") LocalDateTime now,
                                  @Param("oldest") LocalDateTime oldest);

    /**
     * 발송 스레드가 자기 것을 자기가 꺼낼 때 쓴다.
     *
     * JOIN FETCH 가 있어야 트랜잭션 밖에서 alarm.getSchedules() 를 불러도 안 터진다.
     */
    @Query("SELECT a FROM SchedulesAlarm a " +
            "JOIN FETCH a.schedules s " +
            "JOIN FETCH s.users " +
            "WHERE a.schedulesAlarmId = :alarmId")
    Optional<SchedulesAlarm> findByIdWithSchedule(@Param("alarmId") Long alarmId);



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


    /**
     * 시한을 넘긴 알림을 만료로 표시한다.
     *
     * 지금까지는 조회 조건에서 빠지기만 했다. 아무도 안 집으니 PENDING 인 채로 남아
     * 몇 건이 사라졌는지 셀 방법이 없었다. 발송 대상을 집기 전에 먼저 표시한다.
     *
     * 조건이 findTargetAlarmIds 와 같아야 한다. 알림을 꺼둔 사람이나 지워진 일정까지
     * 세면 유실률이 부풀려진다. 유실은 "보냈어야 하는데 못 보낸 것" 만이다.
     *
     * 보내지 않으므로 엔티티를 올릴 이유가 없다. 벌크 UPDATE 한 문장으로 끝내고,
     * 바뀐 행 수가 곧 유실 건수다.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SchedulesAlarm a " +
            "SET a.status = com.toit.notification.alarm.AlarmStatus.EXPIRED " +
            "WHERE a.status = com.toit.notification.alarm.AlarmStatus.PENDING " +
            "AND a.alarmDateTime < :oldest " +
            "AND a.alarmState = true " +
            "AND EXISTS (SELECT s FROM Schedules s " +
            "            JOIN s.users u " +
            "            JOIN UsersSettings us ON us.users = u " +
            "            WHERE s = a.schedules " +
            "            AND s.status = 'ACTIVE' " +
            "            AND us.appAlarmEnabled = true)")
    int markExpired(@Param("oldest") LocalDateTime oldest);


}
