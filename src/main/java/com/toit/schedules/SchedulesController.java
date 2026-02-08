package com.toit.schedules;



import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedules.dto.request.SchedulesDeleteRequest;
import com.toit.schedules.dto.request.SchedulesUpdateRequest;
import com.toit.schedules.dto.response.SchedulesCreateResponse;
import com.toit.schedules.dto.response.SchedulesDeleteResponse;
import com.toit.schedules.dto.response.SchedulesUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class SchedulesController {
    private final SchedulesService schedulesService;


    /***
     * 일정 생성
     */
    @Operation(
            summary = "하나의 일정 생성 - 화면이름 : 캘린더 - 일정추가",
            description = "일정 생성은 POST입니다."
    )
    @PostMapping
    public ResponseEntity<SchedulesCreateResponse> createSchedule(
            @RequestBody SchedulesCreateRequest request
    ) {
        return ResponseEntity.ok(schedulesService.createSchedule(request));
    }

    /***
     * 수정 영역
     */
    @Operation(
            summary = "하나의 일정 수정 - 화면이름 : 캘린더 - 일정 수정",
            description = "일정 수정은 PATCH입니다."
    )
    @PatchMapping
    public ResponseEntity<SchedulesUpdateResponse> updateSchedule(
            @RequestBody SchedulesUpdateRequest request
    ) {
        return ResponseEntity.ok(schedulesService.updateSchedules(request));
    }

    /***
     * 삭제 영역
     */
    @Operation(
            summary = "하나의 일정 삭제 - 화면이름 : 캘린더 - 일정 삭제",
            description = "일정  삭제는 DELETE입니다. 삭제 요청을 보낼 시에 Hard 삭제가 되지 않고 Soft 삭제가 일어납니다."
    )
    @DeleteMapping
    public ResponseEntity<SchedulesDeleteResponse> deleteSchedule(
            @RequestBody SchedulesDeleteRequest request
    ){
        return ResponseEntity.ok(schedulesService.deleteSchedule(request));
    }

}
