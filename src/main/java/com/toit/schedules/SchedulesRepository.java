package com.toit.schedules;

import com.toit.schedules.Schedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SchedulesRepository extends JpaRepository<Schedules, Long> {

}
