package com.toit.items.links;

import com.toit.common.enums.EntityStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinksRepository extends JpaRepository<Links, Long> {

    /**
     * 사용자의 Texts 테이블에서 하나의 폴더에서 여러 개 링크 조회
     */
    @Query("""
        select i
        from Texts i
        where i.users.usersId = :usersId
          and i.storageId = :foldersId
          and i.status = :status
        order by i.createdAt desc
    """)
    List<Links> findLinksInFolders(
            @Param("usersId") Long usersId,
            @Param("foldersId") Long foldersId,
            @Param("status") EntityStatus status
    );
}
