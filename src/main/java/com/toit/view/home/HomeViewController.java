package com.toit.view.home;

import com.toit.view.home.dto.request.HomeViewRequest;
import com.toit.view.home.dto.response.HomeViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page/home")
@RequiredArgsConstructor
public class HomeViewController {

    private final HomeUseCase homeUseCase;

    @GetMapping
    public ResponseEntity<HomeViewResponse> getHome(
            @RequestBody HomeViewRequest request
    ) {
        return ResponseEntity.ok(homeUseCase.getHomeView(request.getUsersId(), request.getTodayDate()));
    }
}