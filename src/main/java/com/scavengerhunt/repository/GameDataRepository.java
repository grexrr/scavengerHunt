package com.scavengerhunt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;

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
        return landmarkRepo.findIdByCity(city);
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

    /**
     * Detect landmark based on player position and view cone.
     * This method provides the functionality that PlayerStateManager needs.
     */
    public Landmark detectLandmark(List<String> candidatesId, Player player) {
        return com.scavengerhunt.utils.GeoUtils.detectedLandmark(candidatesId, player, landmarkRepo);
    }
}
