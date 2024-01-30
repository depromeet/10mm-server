package com.depromeet.global.config.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
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
            e.printStackTrace();
        }
    }
}
