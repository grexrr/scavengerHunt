package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.utils.GeoUtils;


public class PuzzleController {

    private PlayerStateManager playerState;
    private LandmarkManager landmarkManager;
    private Landmark currentTarget;
    private List<Landmark> currentTargetPool = new ArrayList<>(); 
    private List<Landmark> solvedLandmarks = new ArrayList<>(); 

    public PuzzleController(){}
    
    public PuzzleController(Player player, LandmarkManager manager){
        this.playerState = new PlayerStateManager(player, false);
        this.landmarkManager = manager;
    }

    /** 
     * Core Function
     */
    public void startNewRound(){
        this.playerState.resetPlayerTo(
            this.playerState.getPlayer().getLatitude(), 
            this.playerState.getPlayer().getLongitude(), 
            this.playerState.getPlayer().getAngle()
        );
        this.currentTarget = null;
        this.solvedLandmarks.clear();
    }

    public List<Landmark> initTargetPool(double radiusMeters){
        double playerLat = playerState.getPlayer().getLatitude();
        double playerLng = playerState.getPlayer().getLongitude();

        this.currentTargetPool = this.landmarkManager.getAllLandmarksWithinRadius(playerLat, playerLng, radiusMeters);
        return this.currentTargetPool;
    }

    
    public Landmark getCurrentTarget() {
        if (currentTarget == null) {
            if (solvedLandmarks.isEmpty()) {
                // first Riddle based on NearestToPlayer
                this.currentTarget = selectNearestTo(this.playerState.getPlayer().getLatitude(), this.playerState.getPlayer().getLongitude());
            } else {
                // other riddle based on NearestToLastLandmark
                Landmark last = getLastSolved();
                this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
            }
        }
        return this.currentTarget;
    }

   
    public boolean evaluateCurrentTarget(){
        return true;  // default for MVP
    }

    /**
     * Submission: mark current as solved and select next
     */
    public Landmark submitCurrentAnswer(){
        if(this.currentTarget == null){
            return null;
        }
        // Update the solved records for the player and the controller
        playerState.getPlayer().updatePlayerSolvedLandmark(currentTarget);
        this.solvedLandmarks.add(currentTarget);

        // swtich to next
        Landmark last = this.currentTarget;
        this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
        return this.currentTarget;
    }

    public boolean isGameFinish(){
        return getUnsolvedLandmarks().isEmpty();
    }

    // uploading interface
    public void uploadPlayerSolvedLandmark(){
        
    }

    public List<Landmark> getSolvedLandmarks() {
        return solvedLandmarks;
    }

    public List<Landmark> getUnsolvedLandmarks() {
        return this.currentTargetPool.stream()
            .filter(lm -> !solvedLandmarks.contains(lm))
            .collect(Collectors.toList());
    }


    private Landmark getLastSolved() {
        if (this.solvedLandmarks.isEmpty()) return null;
        return this.solvedLandmarks.get(solvedLandmarks.size() - 1);
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
}
