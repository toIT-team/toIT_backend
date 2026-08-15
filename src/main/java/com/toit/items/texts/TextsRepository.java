package com.toit.items.texts;

import com.toit.common.enums.EntityStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TextsRepository extends JpaRepository<Texts, Long> {
    /**
     * 사용자의 Texts 테이블에서 하나의 폴더에서 여러 개 텍스트 조회
     */
    @Query("""
        select i
        from Texts i
        where i.users.usersId = :usersId
          and i.storageId = :foldersId
          and i.status = :status
        order by i.createdAt desc
    """)
    List<Texts> findTextsInFolders(
            @Param("usersId") Long usersId,
            @Param("foldersId") Long foldersId,
            @Param("status") EntityStatus status
    );

    /**
     * 텍스트로 검색
     */
    // MySQL utf8mb4_0900_ai_ci 는 비교 시 대소문자를 구분하지 않으므로 lower() 를 쓰지 않는다.
    // 콜레이션을 _bin/_cs 로 바꾸면 이 검색이 대소문자를 가리게 된다.
    @Query("""
        select t
        from Texts t
        where t.users.usersId = :usersId
          and t.status = :status
          and t.textContent like concat('%', :keyword, '%')
        order by t.createdAt desc
    """)
    List<Texts> searchByTextContent(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );

    /**
     * 텍스트 검색 - 보관함 이름까지 JOIN으로 한 번에 DTO 조회 (N+1 제거)
     */
    @Query("""
        select new com.toit.items.texts.dto.response.TextsGetInFoldersResponse(
            t.textsId, t.storageId, t.textContent, t.createdAt, f.name)
        from Texts t
        join Folders f on f.foldersId = t.storageId
        where t.users.usersId = :usersId
          and t.status = :status
          and t.textContent like concat('%', :keyword, '%')
        order by t.createdAt desc
    """)
    List<com.toit.items.texts.dto.response.TextsGetInFoldersResponse> searchTextsWithFolder(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );

    long countByStorageIdAndStatus(Long storageId, EntityStatus status);

    List<Texts> findByStorageIdAndStatus(Long storageId, EntityStatus status);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Texts t where t.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
