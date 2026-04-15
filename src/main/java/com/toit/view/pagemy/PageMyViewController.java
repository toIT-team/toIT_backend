package com.toit.view.pagemy;

import com.toit.common.SecurityUtil;
import com.toit.view.pagemy.dto.response.PageMyViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages - My", description = "마이페이지 전용 API")
@RestController
@RequestMapping("/page/my")
@RequiredArgsConstructor
public class PageMyViewController {

    private final PageMyUseCase pageMyUseCase;

    @Operation(
            summary = "마이페이지 API - 화면이름 : 마이페이지",
            description = "닉네임, 프로필 이미지, 사용자 이름, 연결된 계정 이메일, 앱 버전을 반환합니다."
    )
    @GetMapping
    public ResponseEntity<PageMyViewResponse> getMyPage() {
        Long usersId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(pageMyUseCase.getMyView(usersId));
    }

}