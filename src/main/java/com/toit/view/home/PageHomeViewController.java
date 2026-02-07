package com.toit.view.home;

import com.toit.view.home.dto.response.PageHomeViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Home", description = "보관함 화면(page) 전용 API")
@RestController
@RequestMapping("/page/home")
@RequiredArgsConstructor
public class PageHomeViewController {

    private final PageHomeUseCase pageHomeUseCase;

    @Operation(
            summary = "Home 화면 API - 화면이름 : 홈-일정, 홈-일정-연속, 홈-기본, 홈-일정"
    )
    @GetMapping
    public ResponseEntity<PageHomeViewResponse> getHome(
            @RequestParam("usersId") Long usersId,
            @RequestParam("todayDate") LocalDate todayDate
    ){
        return ResponseEntity.ok(pageHomeUseCase.getHomeView(usersId, todayDate));
    }
}