package com.toit.schedules;



import com.toit.schedules.dto.request.SchedulesCreateRequest;
import com.toit.schedules.dto.request.SchedulesTodayRequest;
import com.toit.schedules.dto.response.SchedulesCreateResponse;
import com.toit.schedules.dto.response.SchedulesTodayResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class SchedulesController {
    private final SchedulesService schedulesService;

    /*
     *  조회 (GET)
     */

    /***
     * 오늘 일정 조회
     */
    @GetMapping("/today")
    public ResponseEntity<SchedulesTodayResponse> getTodaySchedules(
            @RequestBody SchedulesTodayRequest request
            ) {
        return ResponseEntity.ok(schedulesService.getTodaySchedules(request.getUsersId(), request.getTodayDate()));
    }

    /*
     * 생성 (POST)
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
