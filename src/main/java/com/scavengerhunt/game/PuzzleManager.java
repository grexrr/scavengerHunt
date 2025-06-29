package com.scavengerhunt.game;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.scavengerhunt.repository.RiddleRepository;

@Component
public class PuzzleManager {
    
    private final RestTemplate restTemplate;
  
    public PuzzleManager(RiddleRepository riddleRepo) {
        this.restTemplate = new RestTemplate(); 
    }

    public String getRiddleForLandmark(String landmarkId) {
        String url = "http://localhost:5001/generate-riddle";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("landmarkId", landmarkId);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("riddle");
        } else {
            return "[Failed to get riddle]";
        }
    }
}


