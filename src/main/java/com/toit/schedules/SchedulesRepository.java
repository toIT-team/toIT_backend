package com.toit.schedules;


import com.toit.common.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulesRepository extends JpaRepository<Schedules, Long> {


    /** 오늘 일정 조회 */
    @Query("SELECT s FROM Schedules s " +
            "WHERE s.users.usersId = :userId " +
            "AND s.status = :status " +
            "AND :targetDate BETWEEN s.startDate AND s.endDate " +
            "ORDER BY s.startTime ASC")
    List<Schedules> findSelectedDaySchedules(@Param("userId") Long userId,
                                             @Param("targetDate") LocalDate targetDate,
                                             @Param("status") EntityStatus status);

    /** 시작날짜 종료날짜 사이 일정 조회 */
    @Query("SELECT s FROM Schedules s " +
            "WHERE s.users.usersId = :userId " +
            "AND s.endDate >= :startDate " +
            "AND s.startDate <= :endDate " +
            "AND s.status = :status " +
            "ORDER BY s.startDate ASC, s.startTime ASC")
    List<Schedules> findSchedulesBetween(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("status") EntityStatus status);


    /***
     *  [추가] 알림 시간이 start와 end 사이인 일정 조회
     *  스케줄러 실행할 메서드
     */
//    @Query("select s from Schedules s join fetch s.users " +
//            "where s.alarmDateTime >= :start and s.alarmDateTime < :end " +
//            "and s.notification = true " +
//            "and s.isSent = false")
//    List<Schedules> findTargetSchedules(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Schedules> findByFolders_FoldersIdAndStatus(Long foldersId, EntityStatus status);

    Optional<Schedules> findBySchedulesIdAndUsers_UsersIdAndStatus(Long schedulesId, Long usersId, EntityStatus status);

    /**
     * 스케줄 제목으로 검색
     */
    // MySQL utf8mb4_0900_ai_ci 는 비교 시 대소문자를 구분하지 않으므로 lower() 를 쓰지 않는다.
    // 콜레이션을 _bin/_cs 로 바꾸면 이 검색이 대소문자를 가리게 된다.
    @Query("""
        select s
        from Schedules s
        where s.users.usersId = :usersId
          and s.status = :status
          and s.title like concat('%', :keyword, '%')
        order by s.startDate desc, s.createdAt desc
    """)
    List<Schedules> searchByTitle(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );


    /**
     * 회원 탈퇴용 - schedules_alarm 삭제 이후, folders 삭제 이전에 호출해야 한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Schedules s where s.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
