package com.toit.items.links;

import com.toit.items.links.dto.request.LinksCreateInFoldersRequest;
import com.toit.items.links.dto.request.LinksDeleteInFoldersRequest;
import com.toit.items.links.dto.request.LinksMoveInFoldersRequest;
import com.toit.items.links.dto.request.LinksPreviewRequest;
import com.toit.items.links.dto.request.LinksUpdateRequest;
import com.toit.items.links.dto.response.LinksCreateInFoldersResponse;
import com.toit.items.links.dto.response.LinksDeleteInFoldersResponse;
import com.toit.items.links.dto.response.LinksMoveInFoldersResponse;
import com.toit.items.links.dto.response.LinksPreviewResponse;
import com.toit.items.links.dto.response.LinksUpdateResponse;
import com.toit.swagger.docs.items.links.LinksCreateApiDocs;
import com.toit.swagger.docs.items.links.LinksDeleteApiDocs;
import com.toit.swagger.docs.items.links.LinksPreviewApiDocs;
import com.toit.swagger.docs.items.links.LinksUpdateApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.toit.common.SecurityUtil;
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
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(linksService.createLinksInFolders(usersId, request.getFoldersIdList(), request.getLinksUrl(), request.getLinksName(),
                                                                    request.getTextContent(), request.getLinksThumbnail()));
    }

    @Operation(
            summary = "자료 링크 삭제 API - 화면이름 : 링크 미트볼메뉴",
            description = "자료 추가는 DELETE입니다."
    )
    @LinksDeleteApiDocs
    @DeleteMapping
    public ResponseEntity<LinksDeleteInFoldersResponse> deleteLinksInFolders(
            @RequestBody LinksDeleteInFoldersRequest request
    ){
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(linksService.deleteLinksInFolders(usersId, request.getFoldersId(), request.getLinksId()));
    }

    /**
     * <h2>items의 link 보관함 이동 컨트롤러</h2>
     */
    @Operation(
            summary = "자료 링크 보관함 이동 API - 화면이름 : 링크 미트볼메뉴",
            description = "자료 추가는 PATCHE입니다."
    )
    @LinksDeleteApiDocs
    @PatchMapping
    public ResponseEntity<LinksMoveInFoldersResponse> moveLinksInFolders(
            @RequestBody LinksMoveInFoldersRequest request
            ){
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(linksService.moveLinksInFolders(usersId, request.getFoldersId(), request.getMoveFoldersId(), request.getLinksId()));
    }

    @Operation(
            summary = "자료 링크 수정 API - 화면이름 : 링크 미트볼메뉴",
            description = "링크 제목과 썸네일을 수정합니다."
    )
    @LinksUpdateApiDocs
    @PatchMapping("/update")
    public ResponseEntity<LinksUpdateResponse> updateLinks(
            @RequestBody LinksUpdateRequest request
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                linksService.updateLinks(
                        usersId,
                        request.getFoldersId(),
                        request.getLinksId(),
                        request.getLinksName(),
                        request.getTextContent()
                )
        );
    }

    /**
     * <h2>링크의 썸네일 및 파비콘 및 본문 추출</h2>
     */
    @Operation(
            summary = "링크 미리보기 추출 API",
            description = "linksUrl을 받아 링크 제목, 본문, 썸네일 또는 파비콘을 추출합니다."
    )
    @LinksPreviewApiDocs
    @PostMapping("/preview")
    public ResponseEntity<LinksPreviewResponse> extractLinksPreview(
            @RequestBody LinksPreviewRequest request
    ) {
        return ResponseEntity.ok(linksService.extractLinksPreview(request.getLinksUrl()));
    }

}
