package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages all landmarks used during the game round.
 * - Load landmarks from data source
 * - Maintain solved/unsolved status
 * - Provide helpers to select next suitable target
 */
public class LandmarkManager {
    // 静态配置读取
    private static final String LANDMARK_PROCESSOR_URL = 
        System.getProperty("landmark.processor.url", "http://landmark-processor:5000");

    private GameDataRepository gameDataRepo;
    private String currentCity;

    private List<Landmark> allRoundLandmarks; //within radius
    private List<String> allLocalLandmarkIds; //all landmark-Ids of current city
    

    public LandmarkManager() {
        this.allRoundLandmarks = new ArrayList<>();
    }

    public LandmarkManager(GameDataRepository gameDataRepo, String city) {
        this.allRoundLandmarks = new ArrayList<>();
        this.gameDataRepo = gameDataRepo;
        this.currentCity = city;
        this.allLocalLandmarkIds = this.gameDataRepo.loadLandmarkIdByCity(city); // mvp style, expandable
        System.out.println("[LandmarkManager] Initialized for city: " + city + " with " + this.allLocalLandmarkIds.size() + " local landmarks");
    }

    public void getRoundLandmarksIdWithinRadius(double lat, double lng, double radiusMeters) {
        System.out.println("[LandmarkManager] Checking radius: " + radiusMeters + "m around (" + lat + ", " + lng + ")");
        
        List<Landmark> filtered = allLocalLandmarkIds.stream()
            .map(id -> gameDataRepo.findLandmarkById(id)) 
            .filter(lm -> lm != null)
            .filter(lm -> {
                double dist = GeoUtils.distanceInMeters(lat, lng, lm.getLatitude(), lm.getLongitude());
                System.out.println("[LandmarkManager] " + lm.getName() + " distance = " + dist + "m");
                return dist <= radiusMeters;
            })
            .collect(Collectors.toList());
        
        ensureLandmarkMeta(filtered);
        
        System.out.println("[LandmarkManager] Selected landmarks in range:");
        for (Landmark landmark : filtered) {
            System.out.println("  - " + landmark.getName());
        }

        this.allRoundLandmarks = filtered;
    }

    private void ensureLandmarkMeta(List<Landmark> landmarks){
        if (landmarks == null || landmarks.isEmpty()) return;
        List<String> ids = landmarks.stream()
            .map(Landmark::getId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        if(ids.isEmpty()) return;

        final String fetchMetaUrl = LANDMARK_PROCESSOR_URL + "/generate-landmark-meta";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("landmarkIds", ids);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try{
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(fetchMetaUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Object status  = body.get("status");
                Object gen     = body.get("generated");
                Object skipped = body.get("skipped");
                Object failed  = body.get("failed");
    
                System.out.println(String.format(
                    "[Landmark Processor] ensure batch size=%d -> status=%s, generated=%s, skipped=%s, failed=%s",
                    ids.size(), status, gen, skipped, failed
                ));
            } else {
                System.out.println("[Landmark Processor] ensure batch : HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("[Landmark Processor] Python backend not available (batch): " + e.getMessage());
        }   
    }

    /**
     * Update the current city and reload local landmark IDs
     */
    public void setCurrentCity(String city) {
        this.currentCity = city;
        this.allLocalLandmarkIds = this.gameDataRepo.loadLandmarkIdByCity(city);
    }

    /**
     * Get the current city
     */
    public String getCurrentCity() {
        return this.currentCity;
    }

    /** 
     * Getter & Setter
     */

    public List<Landmark> getAllRouLandmark(){
        return this.allRoundLandmarks;
    }

    public List<String> getAllLocalLandmarkIds(){
        return this.allLocalLandmarkIds;
    }
}
