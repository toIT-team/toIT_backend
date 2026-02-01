package com.toit.foldersview;

import com.toit.foldersview.dto.request.FoldersViewsRequest;
import com.toit.foldersview.dto.request.RecentFoldersRequest;
import com.toit.foldersview.dto.response.FoldersViewsResponse;
import com.toit.foldersview.dto.response.RecentFoldersResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/foldersviews")
@RequiredArgsConstructor
public class FoldersViewsController {

    private final FoldersViewsService foldersViewsService;

    /**
     * 최근 본 보관함 이력 업데이트 없으면 리소스 생성
     */
    @PostMapping
    public ResponseEntity<FoldersViewsResponse> recordFoldersViews(
            @RequestBody FoldersViewsRequest request
    ){
        return ResponseEntity.ok(foldersViewsService.recordFoldersViews(request));
    }

    /**
     * 최근 본 폴더 최대 4개 까지 보내줌
     */
    @GetMapping
    public ResponseEntity<List<RecentFoldersResponse>> getRecentFolders(
            @RequestBody FoldersViewsRequest request
    ) {
        return ResponseEntity.ok(foldersViewsService.getRecentFolders(request));
    }



}
