package com.toit.items.attachments.dto.response;

import lombok.Getter;

@Getter
public class UploadTimingsDto {
    private final long receiveMs;
    private final long s3Ms;
    private final long presignMs;

    public UploadTimingsDto(long receiveMs, long s3Ms, long presignMs) {
        this.receiveMs = receiveMs;
        this.s3Ms = s3Ms;
        this.presignMs = presignMs;
    }
}