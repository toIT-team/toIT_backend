package com.toit.items.attachments;

import com.toit.items.attachments.dto.response.AttachMentsFilesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesCreateInFoldersResponse;
import com.toit.swagger.docs.items.attachments.AttachMentsFilesUploadApiDocs;
import com.toit.swagger.docs.items.attachments.AttachMentsImagesUploadApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    /**
     * image 추가 컨트롤러
     */
    @Operation(
            summary = "자료 이미지 추가 API - 화면이름 : 자료-이미지저장, 자료-이미지 저장_이미지추가",
            description = "자료 추가는 POST입니다."
    )
    @AttachMentsImagesUploadApiDocs
    @PostMapping("/images")
    public ResponseEntity<List<AttachMentsImagesCreateInFoldersResponse>> createImagesInFolders(
            @RequestBody
            @RequestParam("usersId") Long usersId,
            @RequestParam("foldersIdList") List<Long> foldersIdList,
            @RequestParam("textContent") String textContent,
            @RequestPart("image") MultipartFile image
    ) {

        return ResponseEntity.ok(
                attachmentsService.createImagesInFolders(usersId, foldersIdList, textContent, image)
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
    @PostMapping("files")
    public ResponseEntity<List<AttachMentsFilesCreateInFoldersResponse>> createFilesInFolders(
            @RequestBody
            @RequestParam("usersId") Long usersId,
            @RequestParam("foldersIdList") List<Long> foldersIdList,
            @RequestParam("textContent") String textContent,
            @RequestPart("file") MultipartFile file
    ) {

        return ResponseEntity.ok(
                attachmentsService.createFilesInFolders(usersId, foldersIdList, textContent, file)
        );
    }



}
