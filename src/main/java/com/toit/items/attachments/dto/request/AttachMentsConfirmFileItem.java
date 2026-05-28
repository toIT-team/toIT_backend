package com.toit.items.attachments.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttachMentsConfirmFileItem {
    private String objectKey;
    private String fileName;
    private Double fileSize;
    private String contentType;
    private Long width;
    private Long height;
}
