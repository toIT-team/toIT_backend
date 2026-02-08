package com.toit.view.pagefolders;

import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Folders", description = "Home 화면(page) 전용 API")
@RestController
@RequestMapping("/page/folders")
@RequiredArgsConstructor
public class PageFoldersController {

    private final PageFoldersUseCase pageFoldersUseCase;


    @Operation(
            summary = "보관함 메모 화면 API - 화면이름 : 보관함-더보기-메모보기"
    )
    @GetMapping("/memo")
    public ResponseEntity<PageFoldersMemoResponse> getOneFoldersMemo(
            @RequestParam("usersId") Long usersId,
            @RequestParam("foldersId") Long foldersId
    ) {
        return ResponseEntity.ok(
                pageFoldersUseCase.getOneFoldersMemo(usersId, foldersId)
        );
    }
}
