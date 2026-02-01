package com.toit.foldersview;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FoldersViewsRepository extends JpaRepository<FoldersViews, Long> {
    /**
     * FoldersViews 엔티티에서 users.usersId 가 주어진 usersId 이고
     * folder.foldersId 가 주어진 folderId 인 것 하나를 찾는다
     * @param usersId
     * @param foldersId
     * @return
     */
    Optional<FoldersViews> findByUsers_UsersIdAndFolder_FoldersId(Long usersId, Long foldersId);

    @Query("""
        select fv
        from FoldersViews fv
        where fv.users.usersId = :usersId
        order by fv.lastViewedAt desc
    """)
    List<FoldersViews> findRecentFoldersByUser(
            @Param("usersId") Long usersId,
            Pageable pageable
    );
}