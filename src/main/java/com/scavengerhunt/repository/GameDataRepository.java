package com.scavengerhunt.repository;

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
    private final UserRepository userRepo;

    public GameDataRepository(LandmarkRepository landmarkRepo, UserRepository userRepo) {
        this.landmarkRepo = landmarkRepo;
        this.userRepo = userRepo;
    }

    //Expand to JSON in the future

    public List<Landmark> loadLandmarks() {
        return landmarkRepo.findAll();
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
