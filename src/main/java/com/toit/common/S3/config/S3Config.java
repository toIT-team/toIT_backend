package com.toit.common.S3.config;


import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Config {

    private final S3Template s3Template;
    private final S3Client s3Client;

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

    /**
     * prefix 아래의 모든 객체를 삭제한다. (회원 탈퇴 시 users/{usersId}/ 전체 정리)
     * <p>
     * DB의 objectKey 목록이 아니라 prefix를 기준으로 지우므로,
     * presigned PUT으로 업로드만 되고 confirm 되지 않아 DB에 행이 없는 객체까지 함께 정리된다.
     * <p>
     * ListObjectsV2 / DeleteObjects 모두 한 번에 최대 1000건이라 페이지 단위로 그대로 배치 삭제한다.
     */
    public void deleteByPrefix(String prefix) {
        String continuationToken = null;
        int deletedCount = 0;

        do {
            ListObjectsV2Response listed = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .continuationToken(continuationToken)
                            .build()
            );

            List<ObjectIdentifier> targets = listed.contents().stream()
                    .map(object -> ObjectIdentifier.builder().key(object.key()).build())
                    .toList();

            if (!targets.isEmpty()) {
                s3Client.deleteObjects(
                        DeleteObjectsRequest.builder()
                                .bucket(bucket)
                                .delete(Delete.builder().objects(targets).build())
                                .build()
                );
                deletedCount += targets.size();
            }

            continuationToken = listed.nextContinuationToken();
        } while (continuationToken != null);

        log.info("S3 prefix 삭제 완료 - prefix={}, 삭제 객체 수={}", prefix, deletedCount);
    }

}