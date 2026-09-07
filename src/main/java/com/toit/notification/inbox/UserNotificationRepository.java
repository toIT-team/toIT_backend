package com.toit.notification.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    /**
     * 알림함에 보여줄 목록.
     *
     * 알림함 줄은 발송을 시도하기 전에 만들어진다. 그래서 거르지 않으면 보내다 실패한
     * 것까지 사용자에게 보인다. 안 읽은 개수도 아래와 같은 조건을 쓰므로, 안 걸면
     * "뱃지는 0인데 목록에는 새 알림이 있는" 상태가 된다.
     */
    List<UserNotification> findAllByUsers_UsersIdAndIsSentTrueOrderBySentAtDesc(Long usersId);

    long countByUsers_UsersIdAndIsSentTrueAndIsReadFalse(Long usersId);

    Optional<UserNotification> findByNotificationIdAndUsers_UsersId(Long notificationId, Long usersId);

    Optional<UserNotification> findByIdempotencyKey(String idempotencyKey);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserNotification n where n.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
