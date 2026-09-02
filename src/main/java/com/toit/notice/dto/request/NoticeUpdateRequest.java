package com.toit.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeUpdateRequest {

    @NotBlank(message = "title은 필수 값입니다.")
    private String title;

    @NotBlank(message = "content는 필수 값입니다.")
    private String content;

}
