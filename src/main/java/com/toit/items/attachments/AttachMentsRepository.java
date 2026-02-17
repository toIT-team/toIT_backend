package com.toit.items.attachments;

import com.toit.common.enums.AttachMentsType;
import com.toit.common.enums.EntityStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttachMentsRepository extends JpaRepository<AttachMents, Long> {
    /**
     * 사용자의 attachments 테이블에서 하나의 폴더에서 여러 개의 이미지 조회
     */
    @Query("""
        select i
        from AttachMents i
        where i.users.usersId = :usersId
          and i.attachmentsType = :attachMentsType
          and i.storageId = :foldersId
          and i.status = :status
        order by i.createdAt desc
    """)
    List<AttachMents> findAttachMentsInFolders(
            @Param("usersId") Long usersId,
            @Param("attachMentsType") AttachMentsType attachMentsType,
            @Param("foldersId") Long foldersId,
            @Param("status") EntityStatus status
    );
}
