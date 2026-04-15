package com.toit.storage;

import com.toit.common.SecurityUtil;
import com.toit.storage.dto.StorageUsageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Storage", description = "스토리지 사용량 API")
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "내 스토리지 사용량 조회", description = "로그인한 사용자의 S3 스토리지 사용량을 조회합니다.")
    @GetMapping("/usage")
    public ResponseEntity<StorageUsageResponse> getMyUsage() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(storageService.getMyUsage(usersId));
    }
}