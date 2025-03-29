package com.scavengerhunt.data;

import java.util.ArrayList;
import java.util.List;

import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.PlayerStateManager;

public class GameDataRepo {
    /**
     * Load a static landmark list (MVP version).
     */

     /**
     * Load a static landmark list (MVP version).
     */
    
    //Expand to JSON in the future

    public List<Landmark> loadLandmarks() {
        List<Landmark> landmarks = new ArrayList<>();
        // Example data: Glucksman and Honan Chapel
        landmarks.add(new Landmark(1, "Glucksman Gallery", "Where art hides behind glass and stone", 51.8947384, -8.4903073));
        landmarks.add(new Landmark(2, "Honan Chapel", "Echoes of vows and silent prayer linger here", 51.8935836, -8.49002395));

        return landmarks;
    }

    /**
     * Save player's solved landmark IDs (MVP: print to console).
     */
    public void savePlayerProgress(PlayerStateManager playerState) {
        System.out.println("== Saving Player Progress ==");
        System.out.println("Player ID: " + playerState.getPlayer().getPlayerId());
        System.out.println("Solved Landmarks: " + playerState.getPlayer().getSolvedLandmarkIDs());
        System.out.println("Game Finished: " + playerState.isGameFinished());
    }
}
