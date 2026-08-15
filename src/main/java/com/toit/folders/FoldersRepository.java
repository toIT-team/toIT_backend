package com.toit.folders;

import com.toit.common.enums.EntityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FoldersRepository extends  JpaRepository<Folders, Long>{
    List<Folders> findByUsers_UsersId(Long usersId);

    Optional<Folders> findByFoldersIdAndUsers_UsersId(Long foldersId, Long usersId);

    List<Folders> findByUsers_UsersIdAndStatus(Long usersId, EntityStatus status);

    long countByUsers_UsersIdAndStatus(Long usersId, EntityStatus status);

    @Query("""
        select f
        from Folders f
        where f.users.usersId = :usersId
          and f.status = :status
          and f.lastViewedAt is not null
        order by f.lastViewedAt desc
    """)
    List<Folders> findRecentViewedFolders(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            Pageable pageable
    );

    // MySQL utf8mb4_0900_ai_ci 는 비교 시 대소문자를 구분하지 않으므로 lower() 를 쓰지 않는다.
    // 콜레이션을 _bin/_cs 로 바꾸면 이 검색이 대소문자를 가리게 된다.
    @Query("""
        select f
        from Folders f
        where f.users.usersId = :usersId
          and f.status = :status
          and f.name like concat('%', :keyword, '%')
        order by f.createdAt desc
    """)
    List<Folders> searchByName(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );

    /**
     * 회원 탈퇴용 - schedules.folders_id FK 때문에 일정 삭제 이후에 호출해야 한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Folders f where f.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
