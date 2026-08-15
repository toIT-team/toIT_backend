package com.toit.items.attachments;

import com.toit.common.enums.AttachMentsType;
import com.toit.common.enums.EntityStatus;
import com.toit.common.enums.UploadStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
          and i.uploadStatus = com.toit.common.enums.UploadStatus.CONFIRMED
        order by i.createdAt desc
    """)
    List<AttachMents> findAttachMentsInFolders(
            @Param("usersId") Long usersId,
            @Param("attachMentsType") AttachMentsType attachMentsType,
            @Param("foldersId") Long foldersId,
            @Param("status") EntityStatus status
    );

    /**
     * 단건 조회(삭제·이동·이름 수정). 확정된 첨부만 대상으로 한다.
     */
    @Query("""
        select a
        from AttachMents a
        where a.attachmentsId = :attachmentsId
          and a.users.usersId = :usersId
          and a.uploadStatus = com.toit.common.enums.UploadStatus.CONFIRMED
    """)
    Optional<AttachMents> findByAttachmentsIdAndUsers_UsersId(
            @Param("attachmentsId") Long attachmentsId,
            @Param("usersId") Long usersId
    );

    /**
     * confirm 시 전환할 예약 행 조회.
     *
     * <p>presign 이 폴더 수만큼 예약을 남기므로 결과가 여러 건일 수 있다.
     * 소유자를 함께 조건에 넣어 다른 사용자의 objectKey 로 확정하는 것을 막는다.
     */
    List<AttachMents> findByObjectKeyAndUsers_UsersIdAndUploadStatus(
            String objectKey, Long usersId, UploadStatus uploadStatus
    );

    /**
     * 만료된 업로드 예약 조회. 정리 배치가 S3 객체와 함께 회수한다.
     *
     * <p>회수하지 않으면 confirm 이 오지 않은 예약이 계속 쌓여 사용자 용량을 영구히 점유한다.
     */
    List<AttachMents> findByUploadStatusAndReservedAtBefore(
            UploadStatus uploadStatus, LocalDateTime reservedAt
    );

    /**
     * 같은 S3 객체를 참조하는 행 수. 0이면 S3 객체를 지운다.
     *
     * <p><b>PENDING 을 제외하면 안 된다.</b> 업로드 예약 중인 객체를 지워버리게 된다.
     */
    long countByObjectKeyAndStatus(String objectKey, EntityStatus status);

    /**
     * 보관함 내 첨부 개수. 사용자에게 보이는 값이므로 확정분만 센다.
     */
    @Query("""
        select count(a)
        from AttachMents a
        where a.storageId = :storageId
          and a.status = :status
          and a.uploadStatus = com.toit.common.enums.UploadStatus.CONFIRMED
    """)
    long countByStorageIdAndStatus(
            @Param("storageId") Long storageId,
            @Param("status") EntityStatus status
    );

    List<AttachMents> findByStatus(EntityStatus status);

    /**
     * 보관함 삭제 시 하위 첨부 정리용.
     *
     * <p><b>PENDING 을 제외하면 안 된다.</b> 예약 행이 남아 용량을 영구히 점유한다.
     */
    List<AttachMents> findByStorageIdAndStatus(Long storageId, EntityStatus status);

    /**
     * 사용자 스토리지 사용량 합산.
     *
     * <p><b>PENDING 을 제외하면 안 된다.</b> presign 예약이 용량을 선점하는 것이 이 설계의 핵심이라,
     * 여기서 예약분을 빼면 presign 을 반복 호출하는 것만으로 제한을 넘길 수 있다.
     */
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
    // MySQL utf8mb4_0900_ai_ci 는 비교 시 대소문자를 구분하지 않으므로 lower() 를 쓰지 않는다.
    // 콜레이션을 _bin/_cs 로 바꾸면 이 검색이 대소문자를 가리게 된다.
    @Query("""
        select a
        from AttachMents a
        where a.users.usersId = :usersId
          and a.status = :status
          and a.attachmentsType = :type
          and a.uploadStatus = com.toit.common.enums.UploadStatus.CONFIRMED
          and coalesce(a.fileName, '') like concat('%', :keyword, '%')
        order by a.createdAt desc
    """)
    List<AttachMents> searchByTypeAndFileName(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("type") AttachMentsType type,
            @Param("keyword") String keyword
    );

    /**
     * fileName 검색 - 보관함 이름까지 JOIN으로 한 번에 DTO 조회 (N+1 제거)
     */
    @Query("""
        select new com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse(
            a.attachmentsId, a.storageId, a.attachmentsType, a.objectKey,
            a.attachmentsExtension, a.attachmentsSize, a.fileName, a.createdAt,
            f.name, a.textContent)
        from AttachMents a
        join Folders f on f.foldersId = a.storageId
        where a.users.usersId = :usersId
          and a.status = :status
          and a.attachmentsType = :type
          and a.uploadStatus = com.toit.common.enums.UploadStatus.CONFIRMED
          and coalesce(a.fileName, '') like concat('%', :keyword, '%')
        order by a.createdAt desc
    """)
    List<com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse> searchFilesWithFolder(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("type") AttachMentsType type,
            @Param("keyword") String keyword
    );

    /**
     * 회원 탈퇴용 - S3 객체는 prefix 단위로 별도 삭제한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AttachMents a where a.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
