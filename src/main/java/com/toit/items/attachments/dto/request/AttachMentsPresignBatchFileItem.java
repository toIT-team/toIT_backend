package com.toit.items.attachments.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttachMentsPresignBatchFileItem {
    private String contentType;
    private String fileName;
    private long fileSize;
    private Long width;
    private Long height;
}