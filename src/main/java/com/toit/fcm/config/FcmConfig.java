package com.toit.fcm.config;

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

    // [FCM 비활성화] Firebase 초기화 중단 (키 파일 없어도 서버 기동)
    // @PostConstruct
    public void init() throws IOException {
        // if (FirebaseApp.getApps().isEmpty()) {
        //     FirebaseOptions options = FirebaseOptions.builder()
        //             .setCredentials(GoogleCredentials.fromStream(fcmKeyResource.getInputStream()))
        //             .build();
        //     FirebaseApp.initializeApp(options);
        // }
    }
}
