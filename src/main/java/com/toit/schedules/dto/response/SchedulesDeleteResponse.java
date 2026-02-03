package com.toit.schedules.dto.response;


import com.toit.common.enums.EntityStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesDeleteResponse {

    /** 스케줄 id */
    private Long schedulesId;

    /** 사용자 id */
    private Long userId;

    /** 삭제 형식 */
    private EntityStatus status ;

    public SchedulesDeleteResponse(Long schedulesId, Long userId, EntityStatus status) {
        this.schedulesId = schedulesId;
        this.userId = userId;
        this.status = status;
    }
}
