package com.toit.view.folders;

import com.toit.view.folders.dto.request.PageFoldersMemoRequest;
import com.toit.view.folders.dto.response.PageFoldersMemoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - Home", description = "Home 화면(page) 전용 API")
@RestController
@RequestMapping("/page/folders")
@RequiredArgsConstructor
public class PageFoldersController {

    private final PageFoldersUseCase pageFoldersUseCase;
    @PostMapping("/memo")
    public ResponseEntity<PageFoldersMemoResponse> getOneFoldersMemo(
            @RequestBody PageFoldersMemoRequest request
    ) {
        return ResponseEntity.ok(pageFoldersUseCase.getOneFoldersMemo(request.getUsersId(), request.getFoldersId()));
    }
}
