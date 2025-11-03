package com.scavengerhunt.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.scavengerhunt.model.Landmark;

@Component
public class LandmarkProcessorClient {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public LandmarkProcessorClient(@Value("${landmark.processor.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void ensureLandmarkMeta(List<Landmark> landmarks) {
        if (landmarks == null || landmarks.isEmpty())
            return;
        List<String> ids = landmarks.stream()
                .map(Landmark::getId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty())
            return;

        generateMetaBatch(ids);
    }

    private void generateMetaBatch(List<String> landmarkIds) {
        final String fetchMetaUrl = baseUrl + "/generate-landmark-meta";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("landmarkIds", landmarkIds);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(fetchMetaUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Object status = body.get("status");
                Object gen = body.get("generated");
                Object skipped = body.get("skipped");
                Object failed = body.get("failed");

                System.out.println(String.format(
                        "[Landmark Processor] batch size=%d -> status=%s, generated=%s, skipped=%s, failed=%s",
                        landmarkIds.size(), status, gen, skipped, failed));
            } else {
                System.out.println("[Landmark Processor] batch failed: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("[Landmark Processor] batch error: " + e.getMessage());
        }
    }
}