package com.toit.items;

import com.toit.items.dto.request.ItemsLinkCreateRequest;
import com.toit.items.dto.request.ItemsTextCreateReqeust;
import com.toit.items.dto.response.ItemsTextCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemsController {
    private final ItemsService itemsService;

    /**
     * <h2>itesm의 text 추가 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 텍스트 추가 API - 화면이름 : 자료-노트저장",
            description = "자료 추가는 POST입니다."
    )
    @PostMapping("/text")
    public ResponseEntity<ItemsTextCreateResponse> createFoldersText(
            @RequestBody ItemsTextCreateReqeust request
    ){
        return ResponseEntity.ok(itemsService.createFoldersText(request.getUsersId(), request.getFoldersIdList(), request.getTextContent()));
    }

    /**
     * <h2>itesm의 link 추가 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 링크 추가 API - 화면이름 : 자료-링크저장",
            description = "자료 추가는 POST입니다."
    )
    @PostMapping("/link")
    public ResponseEntity<ItemsTextCreateResponse> createFoldersLink(
            @RequestBody ItemsLinkCreateRequest request
    ){
        return ResponseEntity.ok(itemsService.createFoldersLink(request.getUsersId(), request.getFoldersIdList(), request.getFilePath(), request.getTextContent(), request.getLinkThumbnail()));
    }
}
