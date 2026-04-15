package com.toit.items.attachments;

import com.toit.common.enums.AttachMentsType;
import com.toit.common.enums.EntityStatus;
import java.util.List;
import java.util.Optional;
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

    Optional<AttachMents> findByAttachmentsIdAndUsers_UsersId(Long attachmentsId, Long usersId);

    long countByObjectKeyAndStatus(String objectKey, EntityStatus status);

    long countByStorageIdAndStatus(Long storageId, EntityStatus status);

    List<AttachMents> findByStatus(EntityStatus status);

    @Query("""
        select
            coalesce(sum(case when a.attachmentsType = 'IMAGE' then a.attachmentsSize else 0 end), 0),
            coalesce(sum(case when a.attachmentsType = 'FILE' then a.attachmentsSize else 0 end), 0)
        from AttachMents a
        where a.users.usersId = :usersId
          and a.status = :status
    """)
    List<Object[]> sumAttachmentsSizeByUsersId(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status
    );

    @Query("""
        select
            a.users.usersId,
            a.users.name,
            a.users.email,
            coalesce(sum(case when a.attachmentsType = 'IMAGE' then a.attachmentsSize else 0 end), 0),
            coalesce(sum(case when a.attachmentsType = 'FILE' then a.attachmentsSize else 0 end), 0)
        from AttachMents a
        where a.status = :status
        group by a.users.usersId, a.users.name, a.users.email
        order by sum(a.attachmentsSize) desc
    """)
    List<Object[]> sumAttachmentsSizeGroupByUser(@Param("status") EntityStatus status);

    /**
     * fileName으로 검색
     */
    @Query("""
        select a
        from AttachMents a
        where a.users.usersId = :usersId
          and a.status = :status
          and a.attachmentsType = :type
          and lower(coalesce(a.fileName, '')) like lower(concat('%', :keyword, '%'))
        order by a.createdAt desc
    """)
    List<AttachMents> searchByTypeAndFileName(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("type") AttachMentsType type,
            @Param("keyword") String keyword
    );
}
