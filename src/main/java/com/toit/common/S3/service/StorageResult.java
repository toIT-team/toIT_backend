package com.toit.common.S3.service;

import lombok.Getter;

@Getter
public class StorageResult {

    private final String objectKey;
    private final String presignedUrl;

    public StorageResult(String objectKey, String presignedUrl) {
        this.objectKey = objectKey;
        this.presignedUrl = presignedUrl;
    }
}
