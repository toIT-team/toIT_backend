package com.toit.storage.dto;

public record StorageUsageResponse(
        Double imageUsedBytes,
        Double imageUsedMB,
        Double fileUsedBytes,
        Double fileUsedMB,
        Double totalUsedBytes,
        Double totalUsedMB,
        Double limitBytes,
        Double limitMB,
        Double remainingBytes,
        Double remainingMB
) {
    public static final double LIMIT_BYTES = 5L * 1024 * 1024 * 1024; // 5GB

    public static StorageUsageResponse of(Object[] row) {
        double imageBytes = ((Number) row[0]).doubleValue();
        double fileBytes = ((Number) row[1]).doubleValue();
        double totalBytes = imageBytes + fileBytes;
        double remainingBytes = Math.max(0, LIMIT_BYTES - totalBytes);

        return new StorageUsageResponse(
                imageBytes,
                Math.round(imageBytes / (1024.0 * 1024.0) * 100.0) / 100.0,
                fileBytes,
                Math.round(fileBytes / (1024.0 * 1024.0) * 100.0) / 100.0,
                totalBytes,
                Math.round(totalBytes / (1024.0 * 1024.0) * 100.0) / 100.0,
                LIMIT_BYTES,
                Math.round(LIMIT_BYTES / (1024.0 * 1024.0) * 100.0) / 100.0,
                remainingBytes,
                Math.round(remainingBytes / (1024.0 * 1024.0) * 100.0) / 100.0
        );
    }
}