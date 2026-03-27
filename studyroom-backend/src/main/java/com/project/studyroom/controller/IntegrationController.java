package com.project.studyroom.controller;

import com.project.studyroom.exception.FeatureDisabledException;
import com.project.studyroom.service.ExternalInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    private final ExternalInfoService externalInfoService;

    @Value("${feature.external-fact.enabled:true}")
    private boolean externalFactEnabled;

    public IntegrationController(ExternalInfoService externalInfoService) {
        this.externalInfoService = externalInfoService;
    }

    @GetMapping("/random-fact")
    public ResponseEntity<Map<String, String>> getRandomFact() throws Exception {
        if (!externalFactEnabled) {
            throw new FeatureDisabledException("External fact integration is disabled for this environment.");
        }
        String fact = externalInfoService.fetchRandomFact();
        return ResponseEntity.ok(Map.of(
                "provider", "catfact.ninja",
                "fact", fact
        ));
    }
}
