package com.scavengerhunt.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;

public class PuzzleManager {

    private final GameDataRepository gameDataRepo;
    private final PuzzleAgentClient puzzleAgentClient;
    
    private String sessionId;
    private String language;
    private String style;
    private List<Landmark> targetPool; 

    public PuzzleManager(GameDataRepository gameDataRepo, PuzzleAgentClient puzzleAgentClient) {
        this.gameDataRepo = gameDataRepo;
        this.puzzleAgentClient = puzzleAgentClient;
        this.language = "English";
        this.style = "Medieval";
    }

    public void initialize(String sessionId, List<Landmark> targetPool, String language, String style) {
        this.sessionId = sessionId;
        this.targetPool = targetPool;
        this.language = language != null ? language : "English";
        this.style = style != null ? style : "Medieval";
    }

    public String getRiddleForLandmark(String landmarkId) {
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

        try {
            return puzzleAgentClient.generateRiddle(payload);
        } catch (Exception e) {
            System.out.println("[PuzzleManager] Python backend not available: " + e.getMessage());
            return "Default Riddle";
        }
    }
    
    public void resetPuzzleSession() {
        try {
            puzzleAgentClient.resetSession(this.sessionId);
        } catch (Exception ignored) {
        }
    }
    

    public void storeUserGameRoundStatistics(){

    }

    private double normalizeRating(Double rating, String mode){
        //needs to be change to log 
        // Delta = q_{player} - q_{landmark}
        // d = 100 / (1 + e^(-Delta))
        
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


