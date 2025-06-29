package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.LandmarkRepository;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages the state of a player in the scavenger hunt game.
 */
public class PlayerStateManager {
    // private Landmark currentTarget;
    private Player player;
    private LandmarkManager landmarkManager;
    private LandmarkRepository landmarkRepository;

    private boolean isGameFinish = false;
    
    private Landmark detectedLandmark;
    private List<String> solvedLandmarksId = new ArrayList<>(); // for frontend to render into diff color
 
    public PlayerStateManager(Player player, LandmarkManager landmarkManager, LandmarkRepository landmarkRepository) {
        // this.currentTarget = landmark;
        this.player = player;
        this.landmarkManager = landmarkManager;
        this.landmarkRepository = landmarkRepository;
    }

    /** 
     * Core Functions
     */

    public void updatePlayerPosition(double latitude, double longitude, double angle) {
        this.player.setLatitude(latitude);
        this.player.setLongitude(longitude);
        this.player.setAngle(angle);
        this.player.setPlayerViewCone(latitude, longitude, angle, longitude, angle, 50);
    }

    public void updateDetectedLandmark(){
        setDetectedLandmark(GeoUtils.detectedLandmark(landmarkManager.getAllLocalLandmarkIds(), getPlayer(), landmarkRepository));
    }

    public void updateSolvedLandmarksId(String solvedLandmarksId) {
        this.solvedLandmarksId.add(solvedLandmarksId);
    }

    public void resetGame(){
        this.isGameFinish = false;
        this.detectedLandmark = null;
        this.solvedLandmarksId = new ArrayList<>();
    }

    public void setGameFinished(){this.isGameFinish = true;}

    

    // Getter and Setter

    public Player getPlayer(){return player;}

    public boolean isGameFinished(){return isGameFinish;}

    public Landmark getDetectedLandmark() {
        return detectedLandmark;
    }

    public void setDetectedLandmark(Landmark detectedLandmark) {
        this.detectedLandmark = detectedLandmark;
    }

    public List<String> getSolvedLandmarksId() {
        return solvedLandmarksId;
    }
}   