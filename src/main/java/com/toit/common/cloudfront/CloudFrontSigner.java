package com.toit.common.cloudfront;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CloudFrontSigner {

    @Value("${cloudfront.domain}")
    private String domain;

    @Value("${cloudfront.key-pair-id}")
    private String keyPairId;

    @Value("${cloudfront.private-key-path}")
    private Resource privateKeyResource;

    @Value("${cloudfront.ttl-seconds:900}")
    private int ttlSeconds;

    public String getResizeUrl(String objectKey, int width) {
        return domain + "/resize/" + width + "/" + objectKey;
    }

    public String getOriginalUrl(String objectKey) {
        return domain + "/" + objectKey;
    }

    public Map<String, String> generateSignedCookies(Long userId) {
        try {
            log.info("CloudFront Signed Cookie 발급 - keyPairId: {}, domain: {}", keyPairId, domain);
            long expiry = Instant.now().getEpochSecond() + ttlSeconds;
            String resourceUrl = domain + "/*";

            String policy = "{\"Statement\":[{\"Resource\":\"" + resourceUrl
                    + "\",\"Condition\":{\"DateLessThan\":{\"AWS:EpochTime\":" + expiry + "}}}]}";

            String encodedPolicy = toCloudFrontBase64(policy.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = sign(policy.getBytes(StandardCharsets.UTF_8));
            String signature = toCloudFrontBase64(signatureBytes);

            Map<String, String> cookies = new LinkedHashMap<>();
            cookies.put("CloudFront-Policy", encodedPolicy);
            cookies.put("CloudFront-Signature", signature);
            cookies.put("CloudFront-Key-Pair-Id", keyPairId);
            return cookies;
        } catch (Exception e) {
            log.error("CloudFront Signed Cookie 생성 실패 - cause: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new RuntimeException("CloudFront Signed Cookie 생성 실패", e);
        }
    }

    public String generateSignedUrl(String objectKey) {
        try {
            long expiry = Instant.now().getEpochSecond() + ttlSeconds;
            String resourceUrl = domain + "/" + objectKey;

            String policy = "{\"Statement\":[{\"Resource\":\"" + resourceUrl
                    + "\",\"Condition\":{\"DateLessThan\":{\"AWS:EpochTime\":" + expiry + "}}}]}";

            byte[] signatureBytes = sign(policy.getBytes(StandardCharsets.UTF_8));
            String signature = toCloudFrontBase64(signatureBytes);

            return resourceUrl + "?Expires=" + expiry + "&Signature=" + signature + "&Key-Pair-Id=" + keyPairId;
        } catch (Exception e) {
            log.error("CloudFront 서명 URL 생성 실패 - cause: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new RuntimeException("CloudFront 서명 URL 생성 실패", e);
        }
    }

    private byte[] sign(byte[] data) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        return signer.sign();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String content = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        boolean isPkcs1 = content.contains("BEGIN RSA PRIVATE KEY");

        // -----BEGIN ... ----- 형태의 헤더/푸터를 정규식으로 제거
        String pem = content.replaceAll("-{5}[^-]+-{5}", "").replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(pem);

        if (isPkcs1) {
            keyBytes = pkcs1ToPkcs8(keyBytes);
        }

        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    // PKCS#1(BEGIN RSA PRIVATE KEY) → PKCS#8 DER 변환
    private static byte[] pkcs1ToPkcs8(byte[] pkcs1) {
        byte[] version = {0x02, 0x01, 0x00};
        byte[] algorithmIdentifier = {
            0x30, 0x0d,
            0x06, 0x09, 0x2a, (byte)0x86, 0x48, (byte)0x86, (byte)0xf7, 0x0d, 0x01, 0x01, 0x01,
            0x05, 0x00
        };
        byte[] privateKeyOctetString = wrapTlv((byte) 0x04, pkcs1);
        byte[] inner = concat(version, algorithmIdentifier, privateKeyOctetString);
        return wrapTlv((byte) 0x30, inner);
    }

    private static byte[] wrapTlv(byte tag, byte[] value) {
        byte[] length = encodeLength(value.length);
        byte[] result = new byte[1 + length.length + value.length];
        result[0] = tag;
        System.arraycopy(length, 0, result, 1, length.length);
        System.arraycopy(value, 0, result, 1 + length.length, value.length);
        return result;
    }

    private static byte[] encodeLength(int len) {
        if (len < 128) return new byte[]{(byte) len};
        if (len < 256) return new byte[]{(byte) 0x81, (byte) len};
        return new byte[]{(byte) 0x82, (byte) (len >> 8), (byte) len};
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    // CloudFront 전용 Base64: +→- =→_ /→~
    private String toCloudFrontBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes)
                .replace('+', '-')
                .replace('=', '_')
                .replace('/', '~');
    }
}