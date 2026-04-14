package com.toit.view.pagefolders;

import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.swagger.docs.view.pagefolders.PageFoldersApiDocs;
import com.toit.view.pagefolders.dto.response.PageFoldersFoldersItemResponse;
import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import com.toit.common.SecurityUtil;
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
            summary = "보관함 메모 화면 API - 화면이름 : 보관함-더보기-메모보기, 보관함 내부-상단 케밥 메뉴-메모보기"
    )
    @PageFoldersApiDocs
    @GetMapping("/memo")
    public ResponseEntity<PageFoldersMemoResponse> getOneFoldersMemo(
            @RequestParam("foldersId") Long foldersId
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                pageFoldersUseCase.getOneFoldersMemo(usersId, foldersId)
        );
    }

    @Operation(
            summary = "보관함 전체 조회 API - 화면이름 : 보관함 추가"
    )
    @GetMapping
    public ResponseEntity<List<PageFoldersFoldersItemResponse>> getAllFoldersByUser() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                pageFoldersUseCase.getAllFoldersByUser(usersId)
        );
    }

    @Operation(
            summary = "보관함 검색 API - 화면이름 : 보관함 추가"
    )
    @GetMapping("/search")
    public ResponseEntity<List<FoldersItemResponse>> searchFolders(
            @RequestParam("keyword") String keyword
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                pageFoldersUseCase.searchFolders(usersId, keyword)
        );
    }
}
