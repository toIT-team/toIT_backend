package com.toit.schedules;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SchedulesRepository extends JpaRepository<Schedules, Long> {


    /** 오늘 일정 조회 */
    @Query("SELECT s FROM Schedules s " +
            "WHERE s.users.usersId = :userId " +
            "AND :targetDate BETWEEN s.startDate AND s.endDate " +
            "ORDER BY s.startTime ASC")
    List<Schedules> findSelectedDaySchedules(@Param("userId") Long userId,
                                       @Param("targetDate") LocalDate targetDate);

    /** 시작날짜 종료날짜 사이 일정 조회 */
    @Query("SELECT s FROM Schedules s " +
            "WHERE s.users.usersId = :userId " +
            "AND s.endDate >= :startDate " +
            "AND s.startDate <= :endDate " +
            "ORDER BY s.startDate ASC, s.startTime ASC")
    List<Schedules> findSchedulesBetween(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
