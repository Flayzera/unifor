package com.project.studyroom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ExternalInfoService {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String fetchRandomFact() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://catfact.ninja/fact"))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("External API unavailable at the moment.");
        }

        JsonNode json = objectMapper.readTree(response.body());
        String fact = json.path("fact").asText("").trim();
        if (fact.isEmpty()) {
            throw new IllegalStateException("External API returned an invalid payload.");
        }
        return fact;
    }
}
