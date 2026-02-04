package com.toit.view.schedules;


import com.toit.view.schedules.dto.request.ScheduleSearchRequest;
import com.toit.view.schedules.dto.response.SchedulesSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class SchedulesViewController {

    private final SchedulesUseCase schedulesUseCase;

    @GetMapping("/search")
    public ResponseEntity<SchedulesSearchResponse> getSearchSchedules(
            @RequestBody ScheduleSearchRequest request
    ) {
        return ResponseEntity.ok(schedulesUseCase.
                getSearchSchedulesView(
                        request.getUserId(),
                        request.getStartDate(),
                        request.getEndDate()));
    }
}
