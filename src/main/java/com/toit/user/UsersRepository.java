package com.toit.user;

import java.util.Optional;

import com.toit.common.enums.AuthProvider;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findById(Long usersId);

    /**
     * 사용자 행에 배타 락을 걸고 조회한다. {@code SELECT ... FOR UPDATE}
     *
     * <p>스토리지 용량처럼 "사용자 단위로 합계를 검사한 뒤 저장"하는 흐름은
     * 검사와 저장 사이에 다른 요청이 끼어들면 한도를 넘길 수 있다.
     * 이 조회로 같은 사용자의 요청을 한 줄로 세운다.
     *
     * <p>락은 커밋 시점에 풀리므로 반드시 트랜잭션 안에서 호출해야 한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Users u WHERE u.usersId = :usersId")
    Optional<Users> findByIdForUpdate(@Param("usersId") Long usersId);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByAuthProviderAndProviderUsersId(AuthProvider authProvider, String providerUsersId);

}
