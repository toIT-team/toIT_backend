package com.toit.notification.push;

import com.toit.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, String> {

    /**
     * 다른 기기가 이미 쓰고 있는 토큰인지 본다.
     *
     * fcm_token 에 유니크가 걸려 있어, 남의 줄에 붙은 토큰을 그대로 저장하면
     * 제약에 걸린다. 등록할 때 미리 확인해서 옛 줄을 정리한다.
     */
    Optional<FcmToken> findByFcmToken(String token);

    List<FcmToken> findAllByUsers(Users user);

    /**
     * 오래 안 쓴 토큰 정리용.
     *
     * UNREGISTERED 는 발송을 시도해야만 알 수 있어서, 보낼 일이 없는 사용자의
     * 죽은 토큰은 영영 안 지워진다. 타임스탬프로 걸러 지운다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FcmToken f where f.lastUpdatedAt < :threshold")
    int deleteByLastUpdatedAtBefore(@Param("threshold") LocalDateTime threshold);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FcmToken f where f.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
