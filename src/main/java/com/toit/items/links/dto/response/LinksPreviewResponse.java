package com.toit.items.links.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinksPreviewResponse {
    private String linksName;
    private String textContent;
    private String linksThumbnail;

    public LinksPreviewResponse(String linksName, String textContent, String linksThumbnail) {
        this.linksName = linksName;
        this.textContent = textContent;
        this.linksThumbnail = linksThumbnail;
    }
}
