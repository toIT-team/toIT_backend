package com.toit.aws;

import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
class S3TestController {

    private final S3Uploader s3Uploader;

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestPart("file") MultipartFile file) {
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf(".") + 1))
                .orElse("bin");

        String key = "test/" + UUID.randomUUID() + "." + ext;

        s3Uploader.upload(file, key);

        return Map.of("key", key);
    }

    @GetMapping("/presign")
    public Map<String, String> presign(@RequestParam("key") String key) {

        String url = s3Template
                .createSignedGetURL(bucket, key, Duration.ofMinutes(5))
                .toString();

        return Map.of("url", url);
    }

    @GetMapping("/items/{itemId}/image-url")
    public Map<String, String> getImageUrl(@PathVariable("itemId") Long itemId) {
        // 1) DB에서 itemId에 해당하는 imageKey 조회
        String key = attachmentService.findImageKeyByItemId(itemId);

        // 2) presigned url 발급 (예: 30분)
        String url = s3Template.createSignedGetURL(bucket, key, Duration.ofMinutes(30)).toString();

        return Map.of("url", url);
    }
}