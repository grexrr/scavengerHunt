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

    private int playerId;
    private String nickname;

    private Set<Integer> solvedLandmarkIds = new HashSet<>();
    // private Boolean isGameFinish = false;

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

    public Set<Integer> getSolvedLandmarkIDs(){
        return solvedLandmarkIds;
    }

    public void updatePlayerSolvedLandmark(Landmark landmark){
        getSolvedLandmarkIDs().add(landmark.getId());
    }


    /**
     * Other Info
     */
    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerNickname() {
        return nickname;
    }

    public void setPlayerNickname(String nickname) {
        this.nickname = nickname;
    }    
}
