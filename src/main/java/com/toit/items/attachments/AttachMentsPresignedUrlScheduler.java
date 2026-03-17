package com.toit.items.attachments;

import com.toit.common.S3.config.S3Config;
import com.toit.common.enums.EntityStatus;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachMentsPresignedUrlScheduler {

    private final AttachMentsRepository attachMentsRepository;
    private final S3Config s3Config;

    /**
     * 매 6일마다 ACTIVE 상태의 첨부파일 presigned URL 갱신
     * presigned URL 유효기간이 7일이므로 만료 전에 갱신
     */
    @Scheduled(fixedRate = 1000L * 60 * 60 * 24 * 6) // 6일마다
    public void renewPresignedUrls() {
        List<AttachMents> attachments = attachMentsRepository.findByStatus(EntityStatus.ACTIVE);

        for (AttachMents attachment : attachments) {
            String newPresignedUrl = s3Config.presignGetUrl(
                    attachment.getObjectKey(),
                    Duration.ofDays(7)
            );
            attachment.renewPresignedUrl(newPresignedUrl);
            attachMentsRepository.save(attachment);
        }
    }
}