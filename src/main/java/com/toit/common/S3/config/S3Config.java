package com.toit.common.S3.config;


import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.io.InputStream;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3Config {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public void S3upload(String objectKey, InputStream inputStream, String contentType, long contentLength) {
        s3Template.upload(
                bucket,
                objectKey,
                inputStream,
                ObjectMetadata.builder()
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .build()
        );
    }

    public String presignGetUrl(String objectKey, Duration duration) {
        return s3Template.createSignedGetURL(bucket, objectKey, duration).toString();
    }

    public String presignPutUrl(String objectKey, String contentType, Duration duration) {
        return s3Template.createSignedPutURL(
                bucket,
                objectKey,
                duration,
                ObjectMetadata.builder().contentType(contentType).build(),
                null
        ).toString();
    }

    public boolean exists(String objectKey) {
        return s3Template.objectExists(bucket, objectKey);
    }

    public void delete(String objectKey) {
        s3Template.deleteObject(bucket, objectKey);
    }

}