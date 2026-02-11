package com.toit.items.linkpreview;

import lombok.Getter;

@Getter
public class LinkPreview {
    private final String resolvedUrl;   // 최종 URL(리다이렉트 후)
    private final String title;         // og:title 또는 <title>
    private final String description;   // og:description 또는 meta description
    private final String thumbnailUrl;  // og:image 등

    public LinkPreview(String resolvedUrl, String title, String description, String thumbnailUrl) {
        this.resolvedUrl = resolvedUrl;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }
}