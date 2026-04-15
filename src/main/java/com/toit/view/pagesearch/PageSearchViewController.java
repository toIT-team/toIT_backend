package com.toit.view.pagesearch;

import com.toit.swagger.docs.view.pagesearch.PageSearchApiDocs;
import com.toit.view.pagesearch.dto.response.PageSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.toit.common.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Search", description = "검색 화면(page) 전용 API")
@RestController
@RequestMapping("/page/search")
@RequiredArgsConstructor
public class PageSearchViewController {
    private final PageSearchUseCase pageSearchUseCase;

    @Operation(
            summary = "Serach 화면 API - 화면이름 : 통합검색-검색전, 통합검색-검색후"
    )
    @GetMapping
    @PageSearchApiDocs
    public ResponseEntity<PageSearchResponse> search(
            @RequestParam("keyword") String keyword
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(pageSearchUseCase.search(usersId, keyword));
    }
}
