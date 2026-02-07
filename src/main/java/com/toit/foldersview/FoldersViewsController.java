package com.toit.foldersview;

import com.toit.foldersview.dto.request.FoldersViewsRequest;
import com.toit.foldersview.dto.response.FoldersViewsResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
            summary = "최근 본 보관함 생성 API - 화면이름 : 보관함 내부",
            description = "화면이름이 보관함 내부는 없습니다. 보관함 내부에 들어가실 때 이 API 요청하시면 됩니다."
    )
    @PostMapping
    public ResponseEntity<FoldersViewsResponse> recordFoldersViews(
            @RequestBody FoldersViewsRequest request
    ){
        return ResponseEntity.ok(foldersViewsService.recordFoldersViews(request.getUsersId(), request.getFoldersId()));
    }
}
