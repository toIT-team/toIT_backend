package com.toit.storage;

import com.toit.common.enums.EntityStatus;
import com.toit.items.attachments.AttachMentsRepository;
import com.toit.storage.dto.AdminStorageUsageResponse;
import com.toit.storage.dto.StorageUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final AttachMentsRepository attachMentsRepository;

    public StorageUsageResponse getMyUsage(Long usersId) {
        Object[] row = attachMentsRepository.sumAttachmentsSizeByUsersId(usersId, EntityStatus.ACTIVE);
        return StorageUsageResponse.of(row);
    }

    public List<AdminStorageUsageResponse> getAllUsage() {
        return attachMentsRepository.sumAttachmentsSizeGroupByUser(EntityStatus.ACTIVE)
                .stream()
                .map(AdminStorageUsageResponse::of)
                .toList();
    }
}