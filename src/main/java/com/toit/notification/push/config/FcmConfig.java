package com.toit.notification.push.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FcmConfig {

    @Value("${fcm.key.path}")
    private Resource fcmKeyResource;

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // 타임아웃을 안 잡으면 SDK 기본값(60초)이 걸린다. 응답이 없는 한 건이
            // 1분을 통째로 써서, 그것만으로 스케줄러 주기가 무너진다.
            // 성공이 중앙값 318ms · p95 464ms 라 5초면 넉넉하다.
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(fcmKeyResource.getInputStream()))
                    .setConnectTimeout(3_000)
                    .setReadTimeout(5_000)
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}
