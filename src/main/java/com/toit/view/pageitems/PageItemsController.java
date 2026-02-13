package com.toit.view.pageitems;

import com.toit.view.pageitems.dto.response.PageFoldersInItemsAllResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Items", description = "보관함 내부 화면(page) 전용 API")
@RestController
@RequestMapping("/page/items")
@RequiredArgsConstructor
public class PageItemsController {
    private final PageItemsUseCase pageItemsUseCase;

    @Operation(
            summary = "보관함 내부 API - 화면이름 : 보관함-내부-링크, 보관함-내부-노트, 보관함-내부-파일, 보관함-내부-이미지"
    )
    @GetMapping
    public ResponseEntity<PageFoldersInItemsAllResponse> getOneFoldersMemo(
            @RequestParam("usersId") Long usersId,
            @RequestParam("foldersId") Long foldersId
    ) {
        return ResponseEntity.ok(
                pageItemsUseCase.getOnFoldersInItemsAll(usersId, foldersId)
        );
    }

}
