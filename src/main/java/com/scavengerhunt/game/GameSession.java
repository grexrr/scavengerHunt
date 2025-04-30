package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.utils.GeoUtils;

public class GameSession {
    
    private PlayerStateManager playerState;
    private LandmarkManager landmarkManager;
    // private PuzzleController controller;
    
    private Landmark currentTarget;
    private List<Landmark> currentTargetPool = new ArrayList<>(); 
    private List<Landmark> solvedLandmarks = new ArrayList<>(); 

    
    public GameSession(){}

    public GameSession(PlayerStateManager playerState, LandmarkManager landmarkManager) {
        this.playerState = playerState;
        this.landmarkManager = landmarkManager;
    }

    /** 
     * Core Function
     */

    public void startNewRound(double radiusMeters) {
        this.playerState.resetPlayerTo(
            this.playerState.getPlayer().getLatitude(), 
            this.playerState.getPlayer().getLongitude(), 
            this.playerState.getPlayer().getAngle()
        );

        double lat = this.playerState.getPlayer().getLatitude();
        double lng = this.playerState.getPlayer().getLongitude();
        this.currentTargetPool = landmarkManager.getLocalLandmarksWithinRadius(lat, lng, radiusMeters);

        this.currentTarget = null;
        this.solvedLandmarks.clear();

        System.out.println("[INFO] New game round started.");
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
            // playerState.getPlayer().updatePlayerSolvedLandmark(currentTarget);
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
            this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
        } else {
            this.currentTarget = null;
        }
        return this.currentTarget;
    }

    public boolean isGameFinished() {
        return getUnsolvedLandmarks().isEmpty();
    }

    // public Set<Integer> getSolvedLandmarkIds() {
    //     return playerState.getPlayer().getSolvedLandmarkIDs();
    // }

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

    // private Landmark getLastSolved() {
    //     if (this.solvedLandmarks.isEmpty()) return null;
    //     return this.solvedLandmarks.get(solvedLandmarks.size() - 1);
    // }

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

}


