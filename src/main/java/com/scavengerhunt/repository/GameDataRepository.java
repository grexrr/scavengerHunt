package com.scavengerhunt.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;

@Repository
public class GameDataRepository {
    /**
     * Centralized repository for game data operations.
     * Manages all data access for PlayerStateManager and LandmarkManager.
     */

    private final LandmarkRepository landmarkRepo;
    private final UserRepository userRepo;
    private final RestTemplate restTemplate;

    public GameDataRepository(LandmarkRepository landmarkRepo, UserRepository userRepo) {
        this.landmarkRepo = landmarkRepo;
        this.userRepo = userRepo;
        this.restTemplate = new RestTemplate();
    }

    // ==================== Landmark Operations ====================    
    
    public String initLandmarkDataFromPosition(double lat, double lng) {
        String resolveCityUrl = "http://localhost:5002/resolve-city";
        String fetchLandmarkUrl = "http://localhost:5002/fetch-landmark";
    
        Map<String, Object> payload = Map.of("latitude", lat, "longitude", lng);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
    
        String city = null;
    
        // Step 1: try resolve city from Flask
        try {
            ResponseEntity<Map> resolveResp = restTemplate.postForEntity(resolveCityUrl, entity, Map.class);
            if (resolveResp.getStatusCode().is2xxSuccessful() && resolveResp.getBody() != null) {
                Object cityRaw = resolveResp.getBody().get("city");
                if (cityRaw instanceof String resolvedCity && !resolvedCity.isEmpty()) {
                    city = resolvedCity;
                    System.out.println("[GameDataRepo] Resolved city: " + city);
                }
            }
        } catch (Exception e) {
            System.err.println("[GameDataRepo] resolve-city call failed: " + e.getMessage());
        }
    
        // Step 2: fallback if resolution failed
        if (city == null || city.equals("UnknownCity")) {
            System.err.println("[GameDataRepo] Could not resolve city. Skipping fetch, using 'UnknownCity'.");
            city = "Cork";
        }
    
        // Step 3: try database first
        List<Landmark> landmarks = landmarkRepo.findByCity(city);
        if (landmarks.size() >= 10) {
            System.out.println("[GameDataRepo] Using cached landmarks for city: " + city + " (" + landmarks.size() + ")");
            return city;
        }
    
        // Step 4: fetch if needed
        try {
            System.out.println("[GameDataRepo] Landmark data insufficient (" + landmarks.size() + "), triggering fetch...");
            Map<String, String> cityPayload = Map.of("latitude", String.valueOf(lat), "longitude", String.valueOf(lng));
            HttpEntity<Map<String, String>> fetchEntity = new HttpEntity<>(cityPayload, headers);
            ResponseEntity<Map> fetchResp = restTemplate.postForEntity(fetchLandmarkUrl, fetchEntity, Map.class);
    
            if (fetchResp.getStatusCode().is2xxSuccessful()) {
                System.out.println("[GameDataRepo] Fetch succeeded from Flask.");
            } else {
                System.err.println("[GameDataRepo] Fetch returned non-200: " + fetchResp.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("[GameDataRepo] Fetch from Flask failed: " + e.getMessage());
        }
    
        return city;
    }
    
    

    public List<Landmark> loadLandmarks() {
        return landmarkRepo.findAll();
    }

    public List<String> getAllLandmarkIds() {
        return landmarkRepo.findAllId();
    }

    public Landmark findLandmarkById(String id) {
        return landmarkRepo.findById(id).orElse(null);
    }

    public List<String> loadLandmarkIdByCity(String city) {
        System.out.println("[GameDataRepository] Querying for city: '" + city + "'");
        return landmarkRepo.findByCity(city).stream()
            .map(Landmark::getId)
            .toList();
    }

    public double getLandmarkRatingById(String landmarkId){
        Landmark landmark = findLandmarkById(landmarkId);
        Double rating = (landmark != null) ? landmark.getRating() : null;
        return (rating != null) ? rating : 0.5;
    }
   
    public void updateLandmarkRating(String landmarkId, Double rating) {
        Landmark landmark = findLandmarkById(landmarkId);
        if (landmark != null) {
            if (landmark.getRating() == null) {
                landmark.setRating(0.5); // !!!! init default as 0.5 
            }
            landmark.setRating(rating);
            landmarkRepo.save(landmark);
            System.out.println("[✓] Landmark rating updated: " + rating);
        }
    }

    // public void updateLandmarkUncertainty(String landmarkId, Double uncertainty) {
    //     Landmark landmark = findLandmarkById(landmarkId);
    //     if (landmark != null) {
    //         if (landmark.getUncertainty() == null) {
    //             landmark.setUncertainty(0.5);
    //         }
    //         landmark.setUncertainty(uncertainty);
    //         landmarkRepo.save(landmark);
    //     }
    // }

    public void updateLandmarkLastAnswered(String landmarkId, LocalDateTime time) {
        Landmark landmark = findLandmarkById(landmarkId);
        if (landmark != null) {
            landmark.setLastAnswered(time);
            landmarkRepo.save(landmark);
        }
    }

    // ==================== User Operations ====================

    public User getUserById(String userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public void updateUserRating(String userId, Double rating) {
        User user = getUserById(userId);
        if (user != null) {
            if (user.getRating() == null) {
                user.setRating(0.5);
            }
            user.setRating(rating);
            userRepo.save(user);
            System.out.println("[✓] User rating updated: " + rating);
        }
    }

    // public void updateUserUncertainty(String userId, Double uncertainty) {
    //     User user = getUserById(userId);
    //     if (user != null) {
    //         if (user.getUncertainty() == null) {
    //             user.setUncertainty(0.5);
    //         }
    //         user.setUncertainty(uncertainty);
    //         userRepo.save(user);
    //     }
    // }

    public void updateUserLastGameAt(String userId, LocalDateTime time) {
        User user = getUserById(userId);
        if (user != null) {
            user.setLastGameAt(time);
            userRepo.save(user);
        }
    }

    // ==================== Repositories ====================

    public UserRepository getUserRepo() {
        return this.userRepo;
    }

    public LandmarkRepository getLandmarkRepo() {
        return this.landmarkRepo;
    }
}
