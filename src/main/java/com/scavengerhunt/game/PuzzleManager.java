package com.scavengerhunt.game;

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

        Map<String, Object> payload = new HashMap<>();
        payload.put("landmarkId", landmarkId);
        // put difficulties as well
        double landmarkRating = gameDataRepo.getLandmarkRatingById(landmarkId);
        payload.put("difficulty", normalizeRating(landmarkRating, "sigmoid"));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        
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

    private double normalizeRating(Double rating, String mode){
        if (rating == null || rating.isNaN()) return 50.0; // fallback
    
        String m = (mode == null || mode.isEmpty()) ? "default" : mode;
        double clamppedRating = Math.max(-3.0, Math.min(3.0, rating));
        
        if ("default".equals(m)){
            return (clamppedRating - (-3.0)) / 6.0 * 100.0;
        } else if ("sigmoid".equals(m)){
            double normalized = 1.0 / (1.0 + Math.exp(-clamppedRating));
            return normalized * 100.0;
        } else {
            return 50.0;
        }
    }
}


