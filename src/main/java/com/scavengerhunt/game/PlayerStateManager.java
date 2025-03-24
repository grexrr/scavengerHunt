package com.scavengerhunt.game;

/**
 * Manages the state of a player in the scavenger hunt game.
 */
public class PlayerStateManager {
    private Landmark currentTarget;
    private Player player;
 
    // Getter and Setter

    public void setPlayer(Player player){
        this.player = player;
    }

    public void updateCurrentTarget(Landmark landmark) {
        this.currentTarget = landmark;
    }

    public Landmark getCurrentTarget() {
        return currentTarget;
    }

    public Player getPlayer(){
        return player;
    }

    //Core Function

    public PlayerStateManager(Player player, Landmark landmark) {
        this.currentTarget = landmark;
        this.player = player;
    }

    public void updatePlayerPosition(double latitude, double longitude, double angle) {
        this.player.setLatitude(latitude);
        this.player.setLongitude(longitude);
        this.player.setAngle(angle);
    }

    public void markLandmarkSolved(Landmark landmark) {
        player.markLandmarkSolved(landmark);
    }


    public boolean isLandmarkSolved(Landmark landmark) {
        return player.getSolvedLandmarkIds().contains(landmark.getId());
    }
    

    public boolean isGameFinished() {
        return player.isGameFinished();
    }

    public void finishGame() {
        player.setGameFinished(true);
    }

    public void resetGame() {
        player.reset();
        currentTarget = null;
    }

    public void resetGameTo(double lat, double lng, double angle) {
        player.resetTo(lat, lng, angle);
        currentTarget = null;
    }

}
