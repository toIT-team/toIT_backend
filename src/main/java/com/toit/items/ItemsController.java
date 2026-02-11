package com.toit.items;

import com.toit.items.dto.request.ItemsLinkCreateRequest;
import com.toit.items.dto.request.ItemsTextCreateReqeust;
import com.toit.items.dto.response.ItemsTextCreateResponse;
import com.toit.items.dto.response.itemsLinkCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<itemsLinkCreateResponse> createFoldersLink(
            @RequestBody ItemsLinkCreateRequest request
    ){
        return ResponseEntity.ok(itemsService.createFoldersLink(request.getUsersId(), request.getFoldersIdList(), request.getFilePath()));
    }

    /**
     * <h2>itesm의 Image 추가 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 이미지 추가 API - 화면이름 : 자료-이미지저장",
            description = "자료 추가는 POST입니다."
    )
    @PostMapping(value = "/items/{itemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadItemImage(
            @RequestParam Long usersId,
            @RequestParam Long foldersId,
            @RequestPart("file") MultipartFile file
    ) {
        String key = itemsService.uploadItemImage(usersId, itemId, file);
        String url = itemsService.getPresignedUrl(key); // 또는 응답에서 url 바로 내려줌
        return ResponseEntity.ok(Map.of("key", key, "url", url));
    }
}
