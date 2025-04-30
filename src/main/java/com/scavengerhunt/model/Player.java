package com.scavengerhunt.model;

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
     * Other Info
     */
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerNickname() {
        return nickname;
    }

    public void setPlayerNickname(String nickname) {
        this.nickname = nickname;
    }    
}
