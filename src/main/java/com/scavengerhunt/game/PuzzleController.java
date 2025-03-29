package com.scavengerhunt.game;

import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.utils.GeoUtils;


public class PuzzleController {

    private PlayerStateManager playerState;
    private LandmarkRepo landmarkManager;


    public PuzzleController(Player player, LandmarkRepo manager){
        this.playerState = new PlayerStateManager(player, false);
        this.landmarkManager = manager;
    }

    /** 
     * Core Functions
     */
    
    public void startNewRound(){
        playerState.resetPlayerTo(
            playerState.getPlayer().getLatitude(), 
            playerState.getPlayer().getLongitude(), 
            playerState.getPlayer().getAngle()
            );
    }

    public List<Landmark> getAllTargets(double radiusMeters){
        double playerLat = playerState.getPlayer().getLatitude();
        double playerLng = playerState.getPlayer().getLongitude();
    
        return landmarkManager.getUnsolvedLandmarks().stream()
            .filter(lm -> GeoUtils.distanceInMeters(playerLat, playerLng, lm.getLatitude(), lm.getLongitude()) <= radiusMeters)
            .collect(Collectors.toList());
    }
    

    public Landmark getNextTarget(){
        Landmark next;
        if (landmarkManager.getSolvedLandmarks().isEmpty()) {
            next = landmarkManager.selectNearestToPlayer(playerState);
        } else {
            Landmark last = landmarkManager.getLastSolved();
            next = landmarkManager.selectNextLandmark(last);
        }
        return next;
    }
    
    public boolean isGameFinish(){
        return landmarkManager.getUnsolvedLandmarks().isEmpty();
    }

    public void uploadPlayerSolvedLandmark(){
        // upload landmarkManager.getUnsolvedLandmarks() to backend
    }
}    