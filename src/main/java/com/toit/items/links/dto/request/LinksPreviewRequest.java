package com.toit.items.links.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksPreviewRequest {
    /** 링크 URL */
    private String linksUrl;

    public LinksPreviewRequest(String linksUrl) {
        this.linksUrl = linksUrl;
    }
}
