package com.toit.common.S3.attachmentprocessor;

import com.toit.common.enums.AttachMentsType;
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
                        new IllegalArgumentException("지원하지 않는 타입입니다."));
    }
}