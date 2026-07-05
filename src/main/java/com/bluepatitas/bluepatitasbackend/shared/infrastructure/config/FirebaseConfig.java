package com.bluepatitas.bluepatitasbackend.shared.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;
                
                String firebaseEnv = System.getenv("FIREBASE_CREDENTIALS");
                if (firebaseEnv != null && !firebaseEnv.trim().isEmpty()) {
                    serviceAccount = new java.io.ByteArrayInputStream(firebaseEnv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    log.info("Loaded Firebase credentials from environment variable FIREBASE_CREDENTIALS");
                } else {
                    serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");
                    if (serviceAccount != null) {
                        log.info("Loaded Firebase credentials from classpath file.");
                    }
                }

                if (serviceAccount == null) {
                    log.error("Firebase credentials not found. Neither FIREBASE_CREDENTIALS env var nor firebase-service-account.json file exist.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Application has been initialized successfully.");
            }
        } catch (Exception e) {
            log.error("Error initializing Firebase App", e);
        }
    }
}
