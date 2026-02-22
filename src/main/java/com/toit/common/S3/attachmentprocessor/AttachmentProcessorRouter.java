package com.toit.common.S3.attachmentprocessor;

import com.toit.common.enums.AttachMentsType;
import com.toit.exception.items.attachments.AttachmentReadException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachmentProcessorRouter {

    private final List<AttachmentProcessor> processors;

    public AttachmentProcessor getProcessor(AttachMentsType type) {
        return processors.stream()
                .filter(p -> p.supports() == type)
                .findFirst()
                .orElseThrow(() ->
                        new AttachmentReadException("서버에서 파일을 읽지 못 했거나 size를 읽지 못 했습니다."));
    }
}