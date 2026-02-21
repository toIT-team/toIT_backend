package com.toit.items.texts;

import com.toit.items.texts.dto.request.TextsCreateInFoldersRequest;
import com.toit.items.texts.dto.response.TextsCreateInFoldersResponse;
import com.toit.swagger.docs.items.texts.TextsCreateApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Items Management", description = "보관함 내부 화면(page) 전용 API")
@RestController
@RequestMapping("/texts")
@RequiredArgsConstructor
public class TextsController {

    private final TextsService textsService;

    /**
     * <h2>itesm의 text 추가 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 텍스트 추가 API - 화면이름 : 자료-노트저장",
            description = "자료 추가는 POST입니다."
    )
    @TextsCreateApiDocs
    @PostMapping
    public ResponseEntity<TextsCreateInFoldersResponse> createTextsInFolders(
            @RequestBody TextsCreateInFoldersRequest request
    ){
        return ResponseEntity.ok(textsService.createTextsInFolders(request.getUsersId(), request.getFoldersIdList(), request.getTextContent()));
    }
}
