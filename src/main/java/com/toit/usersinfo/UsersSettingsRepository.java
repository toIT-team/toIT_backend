package com.toit.usersinfo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersSettingsRepository extends JpaRepository<UsersSettings, Long> {
}
