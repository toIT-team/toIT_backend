package com.toit.items.attachments;

import com.toit.items.attachments.dto.response.AttachMentsImagesCreateInFoldersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Items Management", description = "보관함 내부 화면(page) 전용 API")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class AttachMentsController {
    private final AttachMentsService attachmentsService;

    /**
     * image 추가 컨트롤러
     */
    @Operation(
            summary = "자료 이미지 추가 API - 화면이름 : 자료-이미지저장",
            description = "자료 추가는 POST입니다."
    )

    @PostMapping
    //
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



}
