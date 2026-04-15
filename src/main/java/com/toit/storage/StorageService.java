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
        List<Object[]> result = attachMentsRepository.sumAttachmentsSizeByUsersId(usersId, EntityStatus.ACTIVE);
        Object[] row = result.isEmpty() ? new Object[]{0, 0} : result.get(0);
        return StorageUsageResponse.of(row);
    }

    public List<AdminStorageUsageResponse> getAllUsage() {
        return attachMentsRepository.sumAttachmentsSizeGroupByUser(EntityStatus.ACTIVE)
                .stream()
                .map(AdminStorageUsageResponse::of)
                .toList();
    }
}