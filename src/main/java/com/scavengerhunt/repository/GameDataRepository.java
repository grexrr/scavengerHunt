package com.scavengerhunt.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.model.Landmark;

@Repository
public class GameDataRepository {
    /**
     * Load a static landmark list (MVP version).
     */

    private final LandmarkRepository landmarkRepo;
    
 
    public GameDataRepository(LandmarkRepository landmarkRepo) {
        this.landmarkRepo = landmarkRepo;
    }

    //Expand to JSON in the future

    public List<Landmark> loadLandmarks() {
        return landmarkRepo.findAll();
    }

    public List<Landmark> loadLandmarks(double radiusMeter) {
        List<Landmark> landmarks = new ArrayList<>();
        // Example data: Glucksman and Honan Chapel
        landmarks.add(new Landmark(1, "Glucksman Gallery", "Where art hides behind glass and stone", 51.8947384, -8.4903073));
        landmarks.add(new Landmark(2, "Honan Chapel", "Echoes of vows and silent prayer linger here", 51.8935836, -8.49002395));
        landmarks.add(new Landmark(3, "Boole Library", "Numbers and knowledge, my namesake knew", 51.892795899999996,-8.491407089727364));
        return landmarks;
    }

    public void loadPlayer(){

    }

    /**
     * Save player's solved landmark IDs (MVP: print to console).
     */
    public void savePlayerProgress(PlayerStateManager playerState) {
        System.out.println("== Saving Player Progress ==");
        System.out.println("Player ID: " + playerState.getPlayer().getPlayerId());
        // System.out.println("Solved Landmarks: " + playerState.getPlayer().getSolvedLandmarkIDs());
        System.out.println("Game Finished: " + playerState.isGameFinished());
    }
}
