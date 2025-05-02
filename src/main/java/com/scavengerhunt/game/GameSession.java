package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.utils.GeoUtils;

public class GameSession {
    
    private PlayerStateManager playerState;
    private LandmarkManager landmarkManager;
    
    private Landmark currentTarget;
    private List<Landmark> currentTargetPool = new ArrayList<>(); 
    private List<Landmark> solvedLandmarks = new ArrayList<>(); 

    private String userId;

    public GameSession(PlayerStateManager playerState, LandmarkManager landmarkManager, String userId) {
        this.playerState = playerState;
        this.landmarkManager = landmarkManager;
        this.userId = userId;
    }

    /** 
     * Core Function
     */

    public void updatePlayerPosition(double lat, double lng, double angle){
        this.playerState.updatePlayerPosition(lat, lng, angle);
        System.out.println("[Session] Updated position: " + lat + ", " + lng + " @ " + angle);
    }

    public void startNewRound(double radiusMeters) {
        double lat = this.playerState.getPlayer().getLatitude();
        double lng = this.playerState.getPlayer().getLongitude();

        this.currentTargetPool = landmarkManager.getLocalLandmarksWithinRadius(lat, lng, radiusMeters);
        this.currentTarget = null;
        this.solvedLandmarks.clear();

        System.out.println("[Session] New game round started.");
    }


    public Landmark getCurrentTarget() {
        if (this.currentTarget == null) {
            this.currentTarget = selectNextTarget();
        }
        return this.currentTarget;
    }

    public boolean submitCurrentAnswer() {
        if (this.currentTarget == null) return false;

        if (checkAnswerCorrect(this.currentTarget)) {
            this.solvedLandmarks.add(currentTarget);

            if (isGameFinished()) {
                playerState.setGameFinished();
            }

            return true;
        }
        return false;
    }

    public Landmark selectNextTarget() {
        if (!isGameFinished()) {
            Landmark last = this.currentTarget;
            if (last == null){
                double playerLat = playerState.getPlayer().getLatitude();
                double playerLng = playerState.getPlayer().getLongitude();
                
                this.currentTarget = selectNearestTo(playerLat, playerLng);
            } else {
                this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
            }
        } else {
            this.currentTarget = null;
        }
        return this.currentTarget;
    }

    public boolean isGameFinished() {
        return getUnsolvedLandmarks().isEmpty();
    }


    public PlayerStateManager getPlayerState() {
        return this.playerState;
    }

    /** 
     * Helper Functions
     */

    public List<Landmark> getUnsolvedLandmarks() {
        return this.currentTargetPool.stream()
            .filter(lm -> !solvedLandmarks.contains(lm))
            .collect(Collectors.toList());
    }

    private Landmark selectNearestTo(double refLat, double refLng) {
        return getUnsolvedLandmarks().stream()
            .min((l1, l2) -> {
                double d1 = GeoUtils.distanceInMeters(refLat, refLng, l1.getLatitude(), l1.getLongitude());
                double d2 = GeoUtils.distanceInMeters(refLat, refLng, l2.getLatitude(), l2.getLongitude());
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    public boolean checkAnswerCorrect(Landmark landmark) {
        // Future integration: answer evaluator + epistemic 
        return true;  // default true for mvp
    }


    public String getUserId() {
        return userId;
    }
}


