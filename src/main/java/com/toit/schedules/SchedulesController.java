package com.toit.schedules;



import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedules.dto.response.SchedulesCreateResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class SchedulesController {
    private final SchedulesService schedulesService;

    /***
     * 생성 영역
     */

    /***
     * 일정 생성
     */
    @PostMapping
    public ResponseEntity<SchedulesCreateResponse> createSchedule(
            @RequestBody SchedulesCreateRequest request
    ) {
        return ResponseEntity.ok(schedulesService.createSchedule(request));
    }

}
