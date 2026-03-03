package com.toit.items.texts;

import com.toit.items.texts.dto.request.TextsCreateInFoldersRequest;
import com.toit.items.texts.dto.request.TextsDeleteInFoldersRequest;
import com.toit.items.texts.dto.request.TextsMoveInFoldersRequest;
import com.toit.items.texts.dto.request.TextsUpdateRequest;
import com.toit.items.texts.dto.response.TextsCreateInFoldersResponse;
import com.toit.items.texts.dto.response.TextsDeleteInFoldersResponse;
import com.toit.items.texts.dto.response.TextsMoveInFoldersResponse;
import com.toit.items.texts.dto.response.TextsUpdateResponse;
import com.toit.swagger.docs.items.texts.TextsCreateApiDocs;
import com.toit.swagger.docs.items.texts.TextsDeleteApiDocs;
import com.toit.swagger.docs.items.texts.TextsUpdateApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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


    /**
     * <h2>itesm의 text 삭제 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 텍스트 삭제 API - 화면이름 : 노트 미트볼메뉴",
            description = "자료 삭제는 DELETE 입니다."
    )
    @TextsDeleteApiDocs
    @DeleteMapping
    public ResponseEntity<TextsDeleteInFoldersResponse>deleteTextsInFolders(
            @RequestBody TextsDeleteInFoldersRequest request
            ){
        return ResponseEntity.ok(textsService.deleteTextsInFolders(request.getUsersId(), request.getFoldersId(), request.getTextsId()));
    }

    /**
     * <h2>itesm의 text 보관함 이동 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 텍스트 보관함 이동 API - 화면이름 : 노트 미트볼메뉴",
            description = "텍스트를 다른 보관함으로 이동은 PATCH입니다."
    )
    @TextsDeleteApiDocs
    @PatchMapping
    public ResponseEntity<TextsMoveInFoldersResponse> moveTextsInFolders(
            @RequestBody TextsMoveInFoldersRequest request
    ){
        return ResponseEntity.ok(textsService.moveTextsInFolders(request.getUsersId(), request.getFoldersId(), request.getMoveFoldersId(), request.getTextsId()));
    }

    @Operation(
            summary = "자료 텍스트 수정 API - 화면이름 : 노트 수정",
            description = "텍스트 본문을 수정합니다."
    )
    @TextsUpdateApiDocs
    @PatchMapping("/update")
    public ResponseEntity<TextsUpdateResponse> updateTexts(
            @RequestBody TextsUpdateRequest request
    ) {
        return ResponseEntity.ok(
                textsService.updateTexts(
                        request.getUsersId(),
                        request.getFoldersId(),
                        request.getTextsId(),
                        request.getTextContent()
                )
        );
    }



}
