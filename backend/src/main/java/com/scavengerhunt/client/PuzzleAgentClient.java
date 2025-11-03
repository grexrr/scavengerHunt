package com.scavengerhunt.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PuzzleAgentClient {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public PuzzleAgentClient(@Value("${puzzle.agent.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String generateRiddle(Map<String, Object> payload) {
        try {
            String url = baseUrl + "/generate-riddle";
            System.out.println("[PuzzleAgentClient] Calling: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                System.out.println("[PuzzleAgentClient] Non-2xx response or null body");
                return "Default Riddle";
            }
            Object r = resp.getBody().get("riddle");
            return (r instanceof String) ? (String) r : "Default Riddle";
        } catch (Exception e) {
            System.out.println("[PuzzleAgentClient] Error calling puzzle-agent: " + e.getMessage());
            e.printStackTrace();
            return "Default Riddle";
        }
    }

    public void resetSession(String sessionId) {
        try {
            String url = baseUrl + "/reset-session";
            Map<String, Object> payload = new HashMap<>();
            payload.put("session_id", sessionId);
            restTemplate.postForEntity(url, payload, String.class);
        } catch (Exception ignored) {
        }
    }
}