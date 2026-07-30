package com.toit.fcm;

import com.toit.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    // 사용자의 특정 토큰이 이미 있는지 확인 (로그인 시 체크용)
    Optional<FcmToken> findByUsersAndFcmToken(Users users, String token);

    Optional<FcmToken> findByFcmToken(String token);

    List<FcmToken> findAllByUsers(Users user);
//
//    // (선택) 특정 사용자의 모든 토큰 삭제 (로그아웃/회원탈퇴 시)
//    void deleteAllByUser(Users user);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FcmToken f where f.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
