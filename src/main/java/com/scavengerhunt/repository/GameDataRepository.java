package com.scavengerhunt.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

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

    public GameDataRepository(LandmarkRepository landmarkRepo, UserRepository userRepo) {
        this.landmarkRepo = landmarkRepo;
        this.userRepo = userRepo;
    }

    // ==================== Landmark Operations ====================
    
    // Load all landmarks (for MVP version).
    public List<Landmark> loadLandmarks() {
        return landmarkRepo.findAll();
    }

    // Get all landmark IDs.
    public List<String> getAllLandmarkIds() {
        return landmarkRepo.findAllId();
    }

    // Load landmark IDs by city
    public List<String> loadLandmarkIdByCity(String city) {
        System.out.println("[GameDataRepository] Querying for city: '" + city + "'");
        List<Landmark> landmarks = landmarkRepo.findByCity(city);
        List<String> ids = landmarks.stream()
            .map(landmark -> landmark.getId())
            .collect(java.util.stream.Collectors.toList());
        System.out.println("[GameDataRepository] Found " + ids.size() + " landmarks for city: " + city);
        return ids;
    }

    // Find landmark by ID.
    public Landmark findLandmarkById(String id) {
        return landmarkRepo.findById(id).orElse(null);
    }

    public void updateLandmarkRating(String landmarkId, double rating){
        System.out.println("[GameDataRepository] Update Landmark [" + landmarkId + "] Rating: " + rating);
        // implementation empty for now
    }

    public void updateLandmarkLastAnswered(String landmarkId, LocalDateTime lastAnswered){
        System.out.println("[GameDataRepository] Update Landmark [" + landmarkId + "] Last Answered to: " + lastAnswered.toString());
        // implementation empty for now
    }

    public void updateLandmarkUncertainty(String landmarkId, double uncertainty){
        System.out.println("[GameDataRepository] Update Landmark [" + landmarkId + "] Uncertainty: " + uncertainty);
        // implementation empty for now
    }

    // ==================== User Operations ====================

    public User getUserById(String id) {
        return userRepo.findById(id).orElse(null);
    }

    public void updateUserLastGameAt(String uId, LocalDateTime lastGameAt){
        System.out.println("[GameDataRepository] Update User [" + getUserById(uId).getUserId() + "] Last Game to: " + lastGameAt.toString());
        // implementation empty for now
    }

    public void updateUserUncertainty(String uId, double uncertainty){
        System.out.println("[GameDataRepository] Update User [" + getUserById(uId).getUserId() + "] Uncertainty: " + uncertainty);
        // implementation empty for now
    }


    public void updateUserRating(String userId, double rating){
        System.out.println("[GameDataRepository] Update User [" + userId + "] Rating: " + rating);
        // implementation empty for now
    }


    // ==================== getters & setters ====================
    
    public UserRepository getUserRepo() {
        return this.userRepo;
    }

}
