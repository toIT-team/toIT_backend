package com.toit.user;

import java.util.Optional;

import com.toit.common.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findById(Long usersId);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByAuthProviderAndProviderUsersId(AuthProvider authProvider, Long providerUsersId);

}
