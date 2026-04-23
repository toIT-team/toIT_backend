package com.toit.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findAllByUsers_UsersIdOrderByCreatedAtDesc(Long usersId);

    Optional<UserNotification> findByNotificationIdAndUsers_UsersId(Long notificationId, Long usersId);
}
