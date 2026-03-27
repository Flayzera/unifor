package com.project.studyroom.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore getFirestore() throws IOException {
        InputStream serviceAccount;

        String inlineJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (inlineJson != null && !inlineJson.isBlank()) {
            serviceAccount = new ByteArrayInputStream(inlineJson.getBytes(StandardCharsets.UTF_8));
        } else if (credentialsPath != null && !credentialsPath.isBlank()) {
            serviceAccount = new FileInputStream(credentialsPath);
        } else {
            // Fallback local (desenvolvimento): arquivo dentro de src/main/resources
            serviceAccount = new ClassPathResource("study-room-firebase-adminsdk.json").getInputStream();
        }

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if(FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
        return FirestoreClient.getFirestore();
    }

}
