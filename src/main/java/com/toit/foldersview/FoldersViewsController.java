package com.toit.foldersview;

import com.toit.foldersview.dto.request.FoldersViewsRequest;
import com.toit.foldersview.dto.response.FoldersViewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        return ResponseEntity.ok(foldersViewsService.recordFoldersViews(request.getUsersId(), request.getFoldersId()));
    }
}
