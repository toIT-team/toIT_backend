package com.toit.view.home;

import com.toit.view.home.dto.request.PageHomeViewRequest;
import com.toit.view.home.dto.response.PageHomeViewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Folders", description = "보관함 화면(page) 전용 API")
@RestController
@RequestMapping("/page/home")
@RequiredArgsConstructor
public class PageHomeViewController {

    private final PageHomeUseCase pageHomeUseCase;

    @GetMapping
    public ResponseEntity<PageHomeViewResponse> getHome(
            @RequestBody PageHomeViewRequest request
    ) {
        return ResponseEntity.ok(pageHomeUseCase.getHomeView(request.getUsersId(), request.getTodayDate()));
    }
}