package com.toit.notice;


import com.toit.notice.dto.response.NoticeReadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notice", description = "공지사항 관련 API")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회", description = "사용자/관리자가 공지사항 목록을 조회합니다 .")
    @GetMapping
    public ResponseEntity<Page<NoticeReadResponse>> getNotices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(noticeService.getNotices(pageable));
    }

    @Operation(summary = "공지사항 단건 조회", description = "알림함의 딥링크가 가리키는 공지사항 하나를 조회합니다.")
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeReadResponse> getNotice(@PathVariable("noticeId") Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

}
