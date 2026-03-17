package com.toit.attachments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.toit.common.S3.config.S3Config;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PresignedUrlExpiryTest {

    @Autowired
    private S3Config s3Config;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Test
    @DisplayName("만료된 presigned URL로 요청하면 403을 반환한다")
    void expiredPresignedUrl_shouldReturn403() throws Exception {
        // 1. 테스트용 파일 S3 업로드
        String objectKey = "test/presigned-url-expiry-test.txt";
        byte[] content = "test".getBytes();
        s3Config.S3upload(
                objectKey,
                new ByteArrayInputStream(content),
                "text/plain",
                content.length
        );

        // 2. 1초짜리 presigned URL 생성
        String presignedUrl = s3Config.presignGetUrl(objectKey, Duration.ofSeconds(1));

        // 3. 2초 대기 (URL 만료)
        Thread.sleep(2000);

        // 4. 만료된 URL로 요청 → 403 확인
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(presignedUrl))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("=== Presigned URL 만료 테스트 결과 ===");
        System.out.println("presignedUrl: " + presignedUrl);
        System.out.println("status code: " + response.statusCode());
        System.out.println("response body: " + response.body());

        assertEquals(403, response.statusCode());
    }
}