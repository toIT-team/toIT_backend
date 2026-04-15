package com.toit.usersinfo;


import com.toit.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersSettingsRepository extends JpaRepository<UsersSettings, Long> {

    UsersSettings findByUsers_UsersId(Long usersId);
}
