package com.toit.items;

import com.toit.common.enums.EntityStatus;
import com.toit.common.enums.ItemsType;
import com.toit.common.enums.StorageTarget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemsRepository extends JpaRepository<Items, Long>{

    /**
     * 사용자의 하나의 폴더의 자료 List 조회
     */
    @Query("""
        select i
        from Items i
        where i.users.usersId = :usersId
          and i.storageTarget = :storageTarget
          and i.storageId = :foldersId
          and i.itemsType = :itemsType
          and i.status = :status
        order by i.createdAt desc
    """)
    List<Items> findFoldersItems(
            @Param("usersId") Long usersId,
            @Param("storageTarget") StorageTarget storageTarget,
            @Param("foldersId") Long foldersId,
            @Param("itemsType") ItemsType itemsType,
            @Param("status") EntityStatus status
    );
}
