package com.toit.common.S3.attachmentprocessor;

import com.toit.common.enums.AttachMentsType;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentProcessor {
    AttachMentsType supports();
    AttachmentPayload getSizeWithDimensions(MultipartFile file);

    StorageResult S3Store(Long usersId, AttachmentPayload payload);
}