package com.scavengerhunt.game;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a player entity in the game.
 * Contains basic information including position and orientation.
 * Logic and progression are handled by PlayerStateManager.
 */
public class Player {


    private double latitude;
    private double longitude;
    private double angle;

    private String playerId;
    private String nickname;

    private Set<String> solvedLandmarkIds = new HashSet<>();
    private Boolean isGameFinish = false;

    /**
     * Creates a new Player at the specified position and orientation
     * @param latitude The initial latitude position
     * @param longitude The initial longitude position  
     * @param angle The initial facing angle in degrees
     */
    public Player(double latitude, double longitude, double angle) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.angle = angle;
    }

    /**
     * Player Status Manager
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    /**
     * Solved Landmarks managing
     */
    public void markLandmarkSolved(Landmark landmark) {
        solvedLandmarkIds.add(landmark.getId());
    }

    public boolean hasSolved(Landmark landmark) {
        return solvedLandmarkIds.contains(landmark.getId());
    }

    public Set<String> getSolvedLandmarkIds() {
        return solvedLandmarkIds;
    }

    /**
    * Game Status Manager
    */
    public boolean isGameFinished() {
        return isGameFinish;
    }

    public void setGameFinished(boolean gameFinished) {
        this.isGameFinish = gameFinished;
    }

    public void reset() {
        this.isGameFinish = false;
        this.solvedLandmarkIds.clear();
    }
    
    public void resetTo(double latitude, double longitude, double angle) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.angle = angle;
        this.isGameFinish = false;
        this.solvedLandmarkIds.clear();
    }

    /**
     * Other Info
     */
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
}
