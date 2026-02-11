package com.toit.aws;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Uploader {
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String key) {
        try (InputStream is = file.getInputStream()) {
            s3Template.upload(bucket, key, is, ObjectMetadata.builder()
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build()
            );
            return key; // DB에는 key 저장 추천
        } catch (IOException e) {
            throw new RuntimeException("S3 upload failed", e);
        }
    }
}
