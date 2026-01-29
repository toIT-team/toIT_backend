package com.toit.folders.dto.response;

import com.toit.common.enums.EntityStatus;
import com.toit.folders.Folders;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersCreateResponse {
    /** 사용자 ID */
    private Long usersId;

    /**폴더 ID */
    private Long foldersId;

    /** 보관함 이름 */
    private String name;

    /**
     * 보관함 메모
     * 값이 없으면 null
     */
    private String memo;

    /**
     * 기본 폴더 여부
     * 1이면 기본 보관함 0이면 일반 보관함
     */
    private Boolean isDefault;

    /** 보관함 컬러*/
    private String color;

    /** 서비스 상태 (ACTIVE/DELETED) */
    private EntityStatus status;

    /** 생성 시간 */
    private LocalDateTime createdAt;

    /** 즐겨찾기 여부 */
    private Boolean isFavorite;

    public FoldersCreateResponse(Folders folders){
        this.usersId = folders.getUsers().getUsersId();
        this.foldersId = folders.getFoldersId();
        this.name = folders.getName();
        this.memo = folders.getMemo();
        this.isDefault = folders.getIsDefault();
        this.color = folders.getColor();
        this.status = folders.getStatus();
        this.createdAt = folders.getCreatedAt();
        this.isFavorite = folders.getIsFavorite();
    }
}
