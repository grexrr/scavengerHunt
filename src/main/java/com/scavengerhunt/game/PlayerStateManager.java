package com.scavengerhunt.game;

/**
 * Manages the state of a player in the scavenger hunt game.
 */
public class PlayerStateManager {
    // private Landmark currentTarget;
    private Player player;
    private boolean isGameFinish = false;
 
    public PlayerStateManager(Player player, boolean isGameFinish) {
        // this.currentTarget = landmark;
        this.player = player;
        this.isGameFinish = isGameFinish;
    }

    /** 
     * Core Functions
     */

    public void updatePlayerPosition(double latitude, double longitude, double angle) {
        this.player.setLatitude(latitude);
        this.player.setLongitude(longitude);
        this.player.setAngle(angle);
    }

    public void resetPlayerTo(double lat, double lng, double angle) {
        player.setLatitude(lat);
        player.setLongitude(lng);
        player.setAngle(angle);
    }

    // Getter and Setter


    public Player getPlayer(){return player;}

    public void setGameFinished(){this.isGameFinish = true;}

    public boolean isGameFinished(){return isGameFinish;}

}