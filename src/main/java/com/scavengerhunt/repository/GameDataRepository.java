package com.scavengerhunt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.scavengerhunt.game.PlayerStateManager;
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
    
    /**
     * Load all landmarks (for MVP version).
   
    public List<Landmark> loadLandmarks() {
        return landmarkRepo.findAll();
    }

    /**
     * Load landmark IDs by city.
     */
    public List<String> loadLandmarkIdByCity(String city) {
        System.out.println("[GameDataRepository] Querying for city: '" + city + "'");
        List<Landmark> landmarks = landmarkRepo.findByCity(city);
        List<String> ids = landmarks.stream()
            .map(landmark -> landmark.getId())
            .collect(java.util.stream.Collectors.toList());
        System.out.println("[GameDataRepository] Found " + ids.size() + " landmarks for city: " + city);
        return ids;
    }

    /**
     * Find landmark by ID.
     */
    public Optional<Landmark> findLandmarkById(String id) {
        return landmarkRepo.findById(id);
    }

    /**
     * Find landmark name by ID.
     */
    public String findLandmarkNameById(String id) {
        return landmarkRepo.findNameById(id);
    }

    /**
     * Get all landmark IDs.
     */
    public List<String> getAllLandmarkIds() {
        return landmarkRepo.findAllId();
    }

    // ==================== User Operations ====================
    
    /**
     * Get UserRepository for user-specific operations.
     */
    public UserRepository getUserRepo() {
        return this.userRepo;
    }

    public User getUserById(String id) {
        return userRepo.findById(id).orElse(null);
    }

    

    // ==================== Game State Operations ====================
    
    /**
     * Save player's solved landmark IDs and game progress.
     */
    public void savePlayerProgress(PlayerStateManager playerState) {
        System.out.println("== Saving Player Progress ==");
        System.out.println("Player ID: " + playerState.getPlayer().getPlayerId());
        System.out.println("Game Finished: " + playerState.isGameFinished());
        System.out.println("Solved Landmarks: " + playerState.getSolvedLandmarksId());
        System.out.println("Detected Landmark: " + 
            (playerState.getDetectedLandmark() != null ? playerState.getDetectedLandmark().getName() : "None"));
    }
}
