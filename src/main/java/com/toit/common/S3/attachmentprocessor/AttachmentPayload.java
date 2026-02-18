package com.toit.common.S3.attachmentprocessor;

import lombok.Getter;

@Getter
public class AttachmentPayload {
    private final String attachmentsExtension;
    private final byte[] bytes;
    private final String fileName;
    private final Double attachmentsSize;
    private String ext;
    private final Long width;
    private final Long height;

    public AttachmentPayload(
            byte[] bytes,
            Double attachmentsSize,
            String attachmentsExtension,
            String fileName,
            String ext,
            Long width,
            Long height
    ) {
        this.bytes = bytes;
        this.attachmentsSize = attachmentsSize;
        this.attachmentsExtension = attachmentsExtension;
        this.fileName = fileName;
        this.ext = ext;
        this.width = width;
        this.height = height;
    }
}