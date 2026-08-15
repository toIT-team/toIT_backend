package com.toit.items.links;

import com.toit.common.enums.EntityStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinksRepository extends JpaRepository<Links, Long> {

    /**
     * 사용자의 Texts 테이블에서 하나의 폴더에서 여러 개 링크 조회
     */
    @Query("""
        select i
        from Links i
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


    /**
     *
     */
    // MySQL utf8mb4_0900_ai_ci 는 비교 시 대소문자를 구분하지 않으므로 lower() 를 쓰지 않는다.
    // 콜레이션을 _bin/_cs 로 바꾸면 이 검색이 대소문자를 가리게 된다.
    @Query("""
        select l
        from Links l
        where l.users.usersId = :usersId
          and l.status = :status
          and coalesce(l.linksName, '') like concat('%', :keyword, '%')
        order by l.createdAt desc
    """)
    List<Links> searchLinks(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );

    /**
     * 링크 검색 - 보관함 이름까지 JOIN으로 한 번에 DTO 조회 (N+1 제거)
     */
    @Query("""
        select new com.toit.items.links.dto.response.LinksGetInFoldersResponse(
            l.linksId, l.storageId, l.linksName, l.linksUrl, l.linksThumbnail,
            l.textContent, l.createdAt, f.name)
        from Links l
        join Folders f on f.foldersId = l.storageId
        where l.users.usersId = :usersId
          and l.status = :status
          and coalesce(l.linksName, '') like concat('%', :keyword, '%')
        order by l.createdAt desc
    """)
    List<com.toit.items.links.dto.response.LinksGetInFoldersResponse> searchLinksWithFolder(
            @Param("usersId") Long usersId,
            @Param("status") EntityStatus status,
            @Param("keyword") String keyword
    );

    long countByStorageIdAndStatus(Long storageId, EntityStatus status);

    List<Links> findByStorageIdAndStatus(Long storageId, EntityStatus status);

    /**
     * 회원 탈퇴용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Links l where l.users.usersId = :usersId")
    void deleteAllByUsersId(@Param("usersId") Long usersId);
}
