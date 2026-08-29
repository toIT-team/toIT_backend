package com.toit.notice.dto.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeUpdateResponse {

    //공지사항 ID
    private Long noticeId;

    //수정된 제목
    private String title;

    //수정된 내용
    private String content;

    //수정 시간
    private LocalDateTime updatedAt;

    public NoticeUpdateResponse(Long noticeId, String title, String content, LocalDateTime updatedAt) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}
