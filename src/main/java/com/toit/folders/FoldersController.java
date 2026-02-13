package com.toit.folders;

import com.toit.folders.dto.request.FoldersCreateRequest;
import com.toit.folders.dto.request.FoldersDeleteRequest;
import com.toit.folders.dto.request.FoldersUpdateRequest;
import com.toit.folders.dto.response.FoldersCreateResponse;
import com.toit.folders.dto.response.FoldersDeleteResponse;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.folders.dto.response.FoldersUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RestController -> Controller 어노테이션은 View용 여기서 JSON을 반환하려면 @ResponseBody가 있어야함. JSON용 컨트롤러
 */
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FoldersController {

    private final FoldersService foldersService;


    /** Folders 보관함 하나 생성 API */
    @Operation(
            summary = "하나의 보관함 생성 - 화면이름 : 보관함 추가, 보관함 추가-아이콘 설정",
            description = "보관함 생성은 POST입니다."
    )
    @PostMapping
    public ResponseEntity<FoldersCreateResponse> createFolders(
            @RequestBody FoldersCreateRequest request
    ){
        return ResponseEntity.ok(foldersService
                .createFolders(request.getUsersId(), request.getName(), request.getMemo(), request.getColor()));
    }

    /**
     * Folders 보관함 수정
     */
    @Operation(
            summary = "보관함 수정 - 화면이름 : 보관함-더보기-수정, 보관함 내부-상단 케밥 메뉴-보관함 수정",
            description = "보관함 이름, 메모, 색상, 아이콘을 수정합니다. 수정은 Patch입니다."
    )
    @PatchMapping
    public ResponseEntity<FoldersUpdateResponse> updateFolders(
            @RequestBody FoldersUpdateRequest request
            ){
        return ResponseEntity.ok(foldersService.updateFolders(request.getUsersId(), request.getFoldersId(),
                request.getName(), request.getMemo(), request.getColor(), request.getIconIdx()));
    }

    @Operation(
            summary = "보관함 삭제 - 화면이름 : 보관함-더보기-삭제, 보관함 내부-상단 케밥 메뉴-보관함 삭제",
            description = "삭제는 Delete입니다. 삭제 요청을 보낼 시에 Hard 삭제가 되지 않고 Soft 삭제가 일어납니다."
    )
    @DeleteMapping
    public ResponseEntity<FoldersDeleteResponse> deleteFolders(
            @RequestBody FoldersDeleteRequest request
    ){
        return ResponseEntity.ok(foldersService.deleteFolders(request.getUsersId(), request.getFoldersId()));
    }
}
