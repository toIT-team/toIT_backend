package com.toit.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Apple OAuth2 client_secret JWT 생성 서비스
 *
 * Apple은 일반 문자열 대신 ES256으로 서명된 JWT를 client_secret으로 사용합니다.
 * 토큰 교환 요청 시마다 이 메서드로 JWT를 생성하여 사용합니다.
 */
@Slf4j
@Service
public class AppleClientSecretService {

    @Value("${apple.oauth.team-id}")
    private String teamId;

    @Value("${apple.oauth.key-id}")
    private String keyId;

    @Value("${apple.oauth.client-id}")
    private String clientId;

    @Value("${apple.oauth.private-key-path}")
    private Resource privateKeyResource;

    public String generateClientSecret() {
        try {
            PrivateKey privateKey = loadPrivateKey();
            Instant now = Instant.now();

            String jwt = Jwts.builder()
                    .header().add("kid", keyId).and()
                    .issuer(teamId)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(15_777_000L))) // 약 6개월 (Apple 최대 허용)
                    .claim("aud", "https://appleid.apple.com") // Apple은 aud를 문자열로 요구
                    .subject(clientId)
                    .signWith(privateKey, Jwts.SIG.ES256) // Apple 필수: ES256 명시
                    .compact();

            log.info("[Apple] client_secret JWT 생성 완료 - teamId={}, keyId={}, clientId={}", teamId, keyId, clientId);
            return jwt;
        } catch (Exception e) {
            log.error("[Apple] client_secret JWT 생성 실패", e);
            throw new RuntimeException("Apple client_secret JWT 생성 실패", e);
        }
    }

    private PrivateKey loadPrivateKey() throws IOException, Exception {
        String content = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String pem = content
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }
}
