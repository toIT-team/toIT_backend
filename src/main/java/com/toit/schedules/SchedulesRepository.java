package com.toit.schedules;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SchedulesRepository extends JpaRepository<Schedules, Long> {


    /**
     * 특정 유저의 일정 중 오늘 날짜가 포함된(startDate <= today <= endDate) 일정 조회
     */
    List<Schedules> findByUsersUsersIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long usersId,
            LocalDate todayForStart,
            LocalDate todayForEnd
    );

}
