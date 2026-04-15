package com.toit.folders;

import com.toit.common.enums.EntityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("""
        select f
        from Folders f
        where f.users.usersId = :usersId
          and f.status = :status
          and lower(f.name) like lower(concat('%', :keyword, '%'))
        order by f.createdAt desc
    """)
    List<Folders> searchByName(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );
}
