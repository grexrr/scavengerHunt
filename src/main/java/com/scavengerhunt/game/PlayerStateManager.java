package com.scavengerhunt.game;

import java.util.List;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages the state of a player in the scavenger hunt game.
 */
public class PlayerStateManager {
    // private Landmark currentTarget;
    private Player player;
    private LandmarkManager landmarkManager;
    private GameDataRepository gameDataRepository;

    private boolean isGameFinish = false;
    
    private Landmark detectedLandmark;
    // private Map<String, Boolean> solvedLandmarksId = new HashMap<>(); // for frontend to render into diff color
 
    public PlayerStateManager(Player player, LandmarkManager landmarkManager, GameDataRepository gameDataRepository) {
        // this.currentTarget = landmark;
        this.player = player;
        this.landmarkManager = landmarkManager;
        this.gameDataRepository = gameDataRepository;
    }

    /** 
     * Core Functions
     */

    public void updatePlayerPosition(double latitude, double longitude, double angle) {
        this.player.setLatitude(latitude);
        this.player.setLongitude(longitude);
        this.player.setAngle(angle);
        this.player.setPlayerViewCone(latitude, longitude, angle, this.player.getSpanDeg(), this.player.getRadiusMeters(), this.player.getResolution());
        updateDetectedLandmark();
    }

    public void updateDetectedLandmark(){
        List<String> candidateIds = landmarkManager.getAllLocalLandmarkIds();
        System.out.println("[PlayerStateManager] Detecting landmark from " + candidateIds.size() + " local landmarks");
        System.out.println("[PlayerStateManager] Player position: " + getPlayer().getLatitude() + ", " + getPlayer().getLongitude() + " @ " + getPlayer().getAngle());
        
        // Use GeoUtils.detectedLandmark method to update detected landmark
        this.detectedLandmark = null;
        this.detectedLandmark = GeoUtils.detectedLandmark(candidateIds, this.player, gameDataRepository);
        
        if (this.detectedLandmark != null) {
            System.out.println("[PlayerStateManager] Detected landmark: " + this.detectedLandmark.getName());
        } else {
            System.out.println("[PlayerStateManager] No landmark detected");
        }
    }

    // public void updateSolvedLandmarksId(String solvedLandmarksId, boolean isCorrect) {
    //     this.solvedLandmarksId.put(solvedLandmarksId, isCorrect);
    // }

    public void resetGame(){
        this.isGameFinish = false;
        this.detectedLandmark = null;
        // this.solvedLandmarksId = new HashMap<>();
    }

    public void setGameFinished(){this.isGameFinish = true;}

    // Getter and Setter

    public Player getPlayer(){return player;}

    public boolean isGameFinished(){return isGameFinish;}

    public Landmark getDetectedLandmark() {
        return detectedLandmark;
    }

    // public Map<String, Boolean> getSolvedLandmarksId() {
    //     return solvedLandmarksId;
    // }
}   