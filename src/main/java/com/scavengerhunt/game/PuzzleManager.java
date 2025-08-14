package com.scavengerhunt.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;

// @Component
public class PuzzleManager {
   

    private final RestTemplate restTemplate = new RestTemplate();
    private final GameDataRepository gameDataRepo;
    
    private String sessionId;
    private final String language;
    private final String style;
    private List<Landmark> targetPool; 


    public PuzzleManager(GameDataRepository gameDataRepo, String sessionId, List<Landmark> targetPool, String language, String style) {
        this.gameDataRepo = gameDataRepo;
        this.sessionId = sessionId;
        this.targetPool = targetPool;
        this.language = language != null ? language : "English";
        this.style = style != null ? style : "Medieval";
    }

    public String getRiddleForLandmark(String landmarkId) {
        String url = "http://localhost:5001/generate-riddle";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        List<String> poolIds = null;
        if (this.targetPool != null) {
            poolIds = this.targetPool.stream()
                    .map(Landmark::getId)  
                    .toList();
        }
        
        System.out.println("[Debug] Sending puzzlePool: " + poolIds);
        
        Map<String, Object> payload = new HashMap<>();
        
        if (this.sessionId != null && !this.sessionId.isEmpty()) {
            payload.put("sessionId", this.sessionId);
        }
        payload.put("landmarkId", landmarkId);
        payload.put("difficulty", normalizeRating(gameDataRepo.getLandmarkRatingById(landmarkId), "sigmoid"));
        payload.put("language", this.language);
        payload.put("style", this.style);
        payload.put("puzzlePool", poolIds);
    
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
    
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
    
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return "Default Riddle";
            }
    
            Map body = response.getBody();
    
            // Object sid = body.get("session_id");
            // if (sid instanceof String s && (this.sessionId == null || !this.sessionId.equals(s))) {
            //     this.sessionId = s;
            //     System.out.println("[PuzzleManager] New Python session_id: " + this.sessionId);
            // }
    
            Object r = body.get("riddle");
            return (r instanceof String) ? (String) r : "Default Riddle";
    
        } catch (Exception e) {
            System.out.println("[PuzzleManager] Python backend not available: " + e.getMessage());
            return "Default Riddle";
        }
    }
    
    public void resetPuzzleSession() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:5001/reset-session";
            
            Map<String, String> payload = new HashMap<>();
            payload.put("session_id", this.sessionId);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            
            System.out.println("[Debug] Reset PuzzleAgent session: " + this.sessionId);
            System.out.println("[Debug] Reset API response: " + response.getStatusCode() + " -> " + response.getBody());
            
        } catch (Exception e) {
            System.out.println("[Warn] Failed to reset PuzzleAgent session: " + e.getMessage());
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

    public void setTargetPool(List<Landmark> targetPool) {
        this.targetPool = targetPool;
    }

    public String getSessionId() { return sessionId; }
    
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

}


