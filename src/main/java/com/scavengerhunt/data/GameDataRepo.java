package com.scavengerhunt.data;

import java.util.ArrayList;
import java.util.List;

import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.Player;

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
        landmarks.add(new Landmark("1", "Glucksman Gallery", "Where art hides behind glass and stone", 51.8947384, -8.4903073, false));
        landmarks.add(new Landmark("2", "Honan Chapel", "Echoes of vows and silent prayer linger here", 51.8935836, -8.49002395, false));
        return landmarks;
    }

    /**
     * Save player's solved landmark IDs (MVP: print to console).
     */
    public void savePlayerProgress(Player player) {
        System.out.println("== Saving Player Progress ==");
        System.out.println("Player ID: " + player.getPlayerId());
        System.out.println("Solved Landmarks: " + player.getSolvedLandmarkIds());
        System.out.println("Game Finished: " + player.isGameFinished());
    }
}
