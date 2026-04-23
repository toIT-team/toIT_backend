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

    /** 삭제 형식 */
    private EntityStatus status;

    public SchedulesDeleteResponse(Long schedulesId, EntityStatus status) {
        this.schedulesId = schedulesId;
        this.status = status;
    }
}
