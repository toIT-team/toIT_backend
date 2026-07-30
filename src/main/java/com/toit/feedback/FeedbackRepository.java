package com.toit.feedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Feedback> findAllByUsers_UsersIdOrderByCreatedAtDesc(Long usersId, Pageable pageable);

    List<Feedback> findAllByUsers_UsersIdOrderByCreatedAtDesc(Long usersId);

    /**
     * 회원 탈퇴용 - 피드백 내용은 운영 자료로 남기고 작성자만 익명화한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Feedback f set f.users = null where f.users.usersId = :usersId")
    void anonymizeByUsersId(@Param("usersId") Long usersId);

}