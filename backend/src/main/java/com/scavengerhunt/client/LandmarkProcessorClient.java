package com.scavengerhunt.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.scavengerhunt.client.dto.FetchLandmarkRequest;
import com.scavengerhunt.client.dto.FetchLandmarkResponse;
import com.scavengerhunt.model.Landmark;

@Component
public class LandmarkProcessorClient {

    private final RestClient restClient;

    @Autowired
    public LandmarkProcessorClient(@Value("${landmark.processor.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    LandmarkProcessorClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String resolveCity(double lat, double lng) {
        try {
            FetchLandmarkResponse body = restClient.post()
                .uri("/resolve-city")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FetchLandmarkRequest(lat, lng))
                .retrieve()
                .body(FetchLandmarkResponse.class);

                if (body != null && "ok".equals(body.status()) && body.city() != null && !body.city().isEmpty()) { return body.city(); }
        } catch (Exception e) {
            System.err.println("[Landmark Processor] resolve-city call failed: " + e.getMessage());
        }
        return null;
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
        try {
            Map<String, Object> body = restClient.post()
                .uri("/generate-landmark-meta")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("landmark_ids", landmarkIds))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (body != null && "ok".equals(body.get("status"))){
                Object generated = body.get("generated");
                Object skipped = body.get("skipped");
                Object failed = body.get("failed");
                System.out.println(String.format(
                    "[Landmark Processor] batch size=%d -> generated=%s, skipped=%s, failed=%s",
                    landmarkIds.size(), generated, skipped, failed));
            }
        } catch (Exception e) {
            System.out.println("[Landmark Processor] batch error: " + e.getMessage());
        }
    }
}
