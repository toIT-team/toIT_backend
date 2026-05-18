package com.toit.items.attachments;

import com.toit.items.attachments.dto.request.AttachMentsConfirmRequest;
import com.toit.items.attachments.dto.request.AttachMentsPresignRequest;
import com.toit.items.attachments.dto.request.AttachMentsUpdateFileNameRequest;
import com.toit.items.attachments.dto.request.AttachMenetsMoveInFoldersRequest;
import com.toit.items.attachments.dto.response.AttachMentsConfirmResponse;
import com.toit.items.attachments.dto.response.AttachMentsPresignResponse;
import com.toit.items.attachments.dto.response.AttachMentsDeleteInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsMoveInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsUpdateFileNameResponse;
import com.toit.swagger.docs.items.attachments.AttachMentsDeleteApiDocs;
import com.toit.swagger.docs.items.attachments.AttachMentsFilesUploadApiDocs;
import com.toit.swagger.docs.items.attachments.AttachMentsImagesUploadApiDocs;
import com.toit.swagger.docs.items.attachments.AttachMentsPatchApiDocs;
import com.toit.swagger.docs.items.attachments.AttachMentsUpdateFileNameApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import com.toit.common.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Items Management", description = "보관함 내부 화면(page) 전용 API")
@RestController
@RequiredArgsConstructor
public class AttachMentsController {
    private final AttachMentsService attachmentsService;

    @Operation(summary = "S3 직접 업로드용 presigned PUT URL 발급 - IMAGE: 최대 3개, FILE: 1개")
    @PostMapping("/attachments/presign")
    public ResponseEntity<List<AttachMentsPresignResponse>> presignUpload(
            @RequestBody AttachMentsPresignRequest request
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(attachmentsService.presignUpload(usersId, request));
    }

    @Operation(summary = "S3 직접 업로드 완료 후 DB 저장 - Flutter가 S3 PUT 성공 후 호출")
    @PostMapping("/attachments/confirm")
    public ResponseEntity<List<AttachMentsConfirmResponse>> confirmUpload(
            @RequestBody AttachMentsConfirmRequest request
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(attachmentsService.confirmUpload(usersId, request));
    }

    /**
     * image 추가 컨트롤러
     */
    @Operation(
            summary = "자료 이미지 추가 API - 화면이름 : 자료-이미지저장, 자료-이미지 저장_이미지추가",
            description = "자료 추가는 POST입니다."
    )
    @AttachMentsImagesUploadApiDocs
    @PostMapping("/attachments/images")
    public ResponseEntity<List<AttachMentsImagesCreateInFoldersResponse>> createImagesInFolders(
            @RequestParam("foldersIdList") List<Long> foldersIdList,
            @RequestParam("textContent") String textContent,
            @RequestPart("image") MultipartFile image
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                attachmentsService.createImagesInFolders(usersId, foldersIdList, textContent, List.of(image))
        );
    }

    /**
     * file 추가 컨트롤러
     */
    @Operation(
            summary = "자료 파일 추가 API - 화면이름 : 자료-파일저장",
            description = "자료 추가는 POST입니다."
    )
    @AttachMentsFilesUploadApiDocs
    @PostMapping("/attachments/files")
    public ResponseEntity<List<AttachMentsFilesCreateInFoldersResponse>> createFilesInFolders(
            @RequestParam("foldersIdList") List<Long> foldersIdList,
            @RequestParam("textContent") String textContent,
            @RequestPart("file") MultipartFile file
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                attachmentsService.createFilesInFolders(usersId, foldersIdList, textContent, file)
        );
    }

    @Operation(
            summary = "자료 파일 및 이미지 삭제 API - 화면이름 : 파일 미트볼메뉴, 이미지 미트볼메뉴",
            description = "자료 삭제는 DELETE입니다."
    )
    @AttachMentsDeleteApiDocs
    @DeleteMapping("/attachments")
    public ResponseEntity<AttachMentsDeleteInFoldersResponse> deleteAttachments(
            @RequestParam("attachmentsId") Long attachmentsId
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(attachmentsService.deleteAttachments(usersId, attachmentsId));
    }

    @Operation(
            summary = "자료 파일/이미지 보관함 이동 API - 파일 미트볼메뉴, 이미지 미트볼메뉴",
            description = "자료 추가는 PATCHE입니다."
    )
    @PatchMapping("/attachments")
    public ResponseEntity<AttachMentsMoveInFoldersResponse> moveAttachmentsInFolders(
            @RequestBody AttachMenetsMoveInFoldersRequest request
    ){
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(attachmentsService.moveAttachmentsInFolders(usersId, request.getFoldersId(), request.getMoveFoldersId(), request.getAttachmentsId()));
    }

    @Operation(
            summary = "자료 파일 이름 수정 API - 화면이름 : 파일 미트볼메뉴",
            description = "attachmentsType이 FILE인 경우에만 fileName을 수정합니다."
    )
    @AttachMentsUpdateFileNameApiDocs
    @PatchMapping("/attachments/update")
    public ResponseEntity<AttachMentsUpdateFileNameResponse> updateFileName(
            @RequestBody AttachMentsUpdateFileNameRequest request
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(
                attachmentsService.updateFileName(
                        usersId,
                        request.getFoldersId(),
                        request.getAttachmentsId(),
                        request.getFileName(),
                        request.getTextContent()
                )
        );
    }





}
