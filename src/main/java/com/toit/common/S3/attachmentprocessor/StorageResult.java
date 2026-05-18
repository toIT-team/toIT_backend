package com.toit.common.S3.attachmentprocessor;

import lombok.Getter;

@Getter
public class StorageResult {

    private final String objectKey;
    private final String presignedUrl;
    private final long s3UploadMs;
    private final long presignMs;

    public StorageResult(String objectKey, String presignedUrl, long s3UploadMs, long presignMs) {
        this.objectKey = objectKey;
        this.presignedUrl = presignedUrl;
        this.s3UploadMs = s3UploadMs;
        this.presignMs = presignMs;
    }
}