package com.toit.usersinfo;


import com.toit.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersSettingsRepository extends JpaRepository<UsersSettings, Long> {

    UsersSettings findByUsers_UsersId(Long usersId);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UsersSettings us where us.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
