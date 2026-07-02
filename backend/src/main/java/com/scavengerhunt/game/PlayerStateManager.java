package com.scavengerhunt.game;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages the state of a player in the scavenger hunt game.
 */
public class PlayerStateManager {
    private static final Logger log = LoggerFactory.getLogger(PlayerStateManager.class);

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

        // Use GeoUtils.detectedLandmark method to update detected landmark
        this.detectedLandmark = null;
        this.detectedLandmark = GeoUtils.detectedLandmark(candidateIds, this.player, gameDataRepository);

        log.debug("Detected landmark from {} candidates: {}", candidateIds.size(),
            this.detectedLandmark != null ? this.detectedLandmark.getId() : "none");
    }

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
}
