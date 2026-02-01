package com.toit.folders;

import com.toit.folders.dto.request.FoldersAllRequest;
import com.toit.folders.dto.request.FoldersCreateRequest;
import com.toit.folders.dto.response.FoldersAllResponse;
import com.toit.folders.dto.response.FoldersCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    @PostMapping
    public ResponseEntity<FoldersCreateResponse> createFolders(
            @RequestBody FoldersCreateRequest request
    ){
        return ResponseEntity.ok(foldersService.createFolders(request));
    }

    /**
     * <h2>Folders 보관함 전체 조회 API</h2>
     *
     * <p>한 명의 사용자가 접근 가능한 모든 보관함을 조회합니다.</p>
     */
    @GetMapping("/all")
    public ResponseEntity<FoldersAllResponse> getAllFoldersByUser(
            @RequestBody FoldersAllRequest request
    ){
        return ResponseEntity.ok(foldersService.getAllFoldersByUser(request));
    }
}
