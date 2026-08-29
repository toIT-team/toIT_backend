package com.toit.items.attachments.storage.dto;

public record AdminStorageUsageResponse(
        Long usersId,
        String name,
        String email,
        Double imageUsedBytes,
        Double imageUsedMB,
        Double fileUsedBytes,
        Double fileUsedMB,
        Double totalUsedBytes,
        Double totalUsedMB
) {
    public static AdminStorageUsageResponse of(Object[] row) {
        double imageBytes = ((Number) row[3]).doubleValue();
        double fileBytes = ((Number) row[4]).doubleValue();
        double totalBytes = imageBytes + fileBytes;

        return new AdminStorageUsageResponse(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                imageBytes,
                Math.round(imageBytes / (1024.0 * 1024.0) * 100.0) / 100.0,
                fileBytes,
                Math.round(fileBytes / (1024.0 * 1024.0) * 100.0) / 100.0,
                totalBytes,
                Math.round(totalBytes / (1024.0 * 1024.0) * 100.0) / 100.0
        );
    }
}