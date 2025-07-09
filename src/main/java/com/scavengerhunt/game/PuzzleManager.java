package com.scavengerhunt.game;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;

@Component
public class PuzzleManager {
    
    private final RestTemplate restTemplate;
    private GameDataRepository gameDataRepo;

    private LocalDateTime sessionTimeId;
    private Map<String, ?> gameRoundStdMap;
    private LocalDateTime lastPuzzleStartTime;
  
    public PuzzleManager(GameDataRepository gameDataRepo) {
        this.restTemplate = new RestTemplate();
        this.gameDataRepo = gameDataRepo; 
    }

    public String getRiddleForLandmark(String landmarkId) {
        String url = "http://localhost:5001/generate-riddle";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("landmarkId", landmarkId);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("riddle");
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("[PuzzleManager] Python backend not available, returning null: " + e.getMessage());
            return null;
        }
    }

    public void storeUserGameRoundStatistics(){

    }

    public void startPuzzleTimer(Landmark currentLandmark){
        System.out.println("[Puzzle Manager] Start Timing for" + currentLandmark.getName());
        lastPuzzleStartTime = LocalDateTime.now();
    }

    public void pausePuzzleTimer(Landmark currentLandmark){
        System.out.println("[Puzzle Manager] Pausing Timing for" + currentLandmark.getName());
        long durationSeconds = Duration.between(lastPuzzleStartTime, LocalDateTime.now()).getSeconds();
        lastPuzzleStartTime = LocalDateTime.now();
        // add info to the round map
    }
}


