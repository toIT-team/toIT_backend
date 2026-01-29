package com.toit.folders.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersCreateRequest {

    /** 사용자 ID */
    private Long usersId;

    /** 보관함 이름 */
    private String name;

    /**
     * 보관함 메모
     * 값이 없으면 null
     */
    private String memo;

    /** 보관홤 컬러 */
    private String color;
}