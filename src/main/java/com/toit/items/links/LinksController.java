package com.toit.items.links;

import com.toit.items.links.dto.request.LinksCreateInFoldersRequest;
import com.toit.items.links.dto.request.LinksDeleteInFoldersRequest;
import com.toit.items.links.dto.response.LinksCreateInFoldersResponse;
import com.toit.items.links.dto.response.LinksDeleteInFoldersResponse;
import com.toit.swagger.docs.items.links.LinksCreateApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Items Management", description = "보관함 내부 화면(page) 전용 API")
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksController {

    private final LinksService linksService;

    /**
     * <h2>links를 폴더에  추가 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 링크 추가 API - 화면이름 : 자료-링크저장",
            description = "자료 추가는 POST입니다."
    )
    @LinksCreateApiDocs
    @PostMapping
    public ResponseEntity<LinksCreateInFoldersResponse> createLinksInFolders(
            @RequestBody LinksCreateInFoldersRequest request
    ){
        return ResponseEntity.ok(linksService.createLinksInFolders(request.getUsersId(), request.getFoldersIdList(), request.getLinksUrl()));
    }

    @Operation(
            summary = "자료 링크 삭제 API - 화면이름 : 링크 미트볼메뉴",
            description = "자료 추가는 DELETE입니다."
    )
    @LinksCreateApiDocs
    @DeleteMapping
    public ResponseEntity<LinksDeleteInFoldersResponse> deleteLinksInFolders(
            @RequestBody LinksDeleteInFoldersRequest request
    ){
        return ResponseEntity.ok(linksService.deleteLinksInFolders(request.getUsersId(), request.getFoldersId(), request.getLinksId()));
    }
}
