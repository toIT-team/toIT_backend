package com.toit.items.attachments.dto.response;

import lombok.Getter;

@Getter
public class AttachMentsPresignResponse {
    private final String objectKey;
    private final String uploadUrl;
    private final int expiresInSeconds;

    public AttachMentsPresignResponse(String objectKey, String uploadUrl, int expiresInSeconds) {
        this.objectKey = objectKey;
        this.uploadUrl = uploadUrl;
        this.expiresInSeconds = expiresInSeconds;
    }
}
