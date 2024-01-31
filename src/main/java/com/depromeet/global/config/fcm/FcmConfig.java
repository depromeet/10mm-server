package com.depromeet.global.config.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FcmConfig {

    @Value("${fcm.certification}")
    private String fcmCertification;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options =
                        new FirebaseOptions.Builder()
                                .setCredentials(
                                        GoogleCredentials.fromStream(
                                                new ByteArrayInputStream(
                                                        fcmCertification.getBytes(
                                                                StandardCharsets.UTF_8))))
                                .build();
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            log.info("FCM initializing Exception: {}", e.getStackTrace()[0]);
        }
    }
}
