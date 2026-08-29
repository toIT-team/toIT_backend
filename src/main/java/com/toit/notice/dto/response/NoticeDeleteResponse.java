package com.toit.notice.dto.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeDeleteResponse {

    private Long noticeId;

    public NoticeDeleteResponse(Long noticeId) {
        this.noticeId = noticeId;
    }
}
