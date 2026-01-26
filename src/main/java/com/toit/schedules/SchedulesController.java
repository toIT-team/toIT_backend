package com.toit.schedules;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class SchedulesController {
    private final SchedulesService schedulesService;

    public SchedulesController(SchedulesService schedulesService) {
        this.schedulesService = schedulesService;
    }

    /***
     *
     * 메인 페이지에서 조회할 오늘의 스케줄 정보
     *
     */
//    @GetMapping("/api/v1/schedules")
//    public ResponseEntity<List<SchedulesResponseDto>> getRemainingSchedules(
//            @RequestParam Long userId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime baseDateTime
//    ) {
//        // baseDateTime이 없으면 현재 서버 시간을 기준으로 설정
//        LocalDateTime criteriaTime = (baseDateTime != null) ? baseDateTime : LocalDateTime.now();
//
//        List<SchedulesResponseDto> schedules = schedulesService.findRemainingSchedules(userId, criteriaTime);
//
//        return ResponseEntity.ok(schedules);
//    }
}
