package com.scavengerhunt.game;

import java.util.HashSet;
import java.util.Set;

public class PlayerStateManager {
    private Landmark currentTarget;
    private Set<String> solvedLandmarkIds;
    private double playerLatitude;
    private double playerLongitude;
    private double playerAngle;

    public PlayerStateManager() {
        this.solvedLandmarkIds = new HashSet<>();
    }

    public void setCurrentTarget(Landmark landmark) {
        this.currentTarget = landmark;
    }

    //set current target to solve
    public Landmark getCurrentTarget(){
        return currentTarget;
    }

    //mark the landmark is solved
    public void markLandmarkSolved(Landmark landmark){
        solvedLandmarkIds.add(landmark.getId());
    }

    public boolean isLandmarkSolved(Landmark landmark){
        return solvedLandmarkIds.contains(landmark.getId());
    }


    public void updatePlayerPosition(double latitude, double longitude){
        this.playerLatitude = latitude;
        this.playerLongitude = longitude;
    }

    public void updatePlayerAngle(double angle) {
        this.playerAngle = angle;
    }

    public double getPlayerLatitude() { return playerLatitude; }
    public double getPlayerLongitude() { return playerLongitude; }
    public double getPlayerAngle() { return playerAngle; }
}
