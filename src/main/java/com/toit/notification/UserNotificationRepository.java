package com.toit.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findAllByUsers_UsersIdOrderByCreatedAtDesc(Long usersId);

    long countByUsers_UsersIdAndIsSentTrueAndIsReadFalse(Long usersId);

    Optional<UserNotification> findByNotificationIdAndUsers_UsersId(Long notificationId, Long usersId);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserNotification n where n.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
