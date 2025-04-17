package com.scavengerhunt.game;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class GameSession {
    
    private PlayerStateManager playerState;
    private PuzzleController controller;
    private LandmarkRepo landmarkRepo;
    
    public GameSession(){}
    
    public GameSession(LandmarkRepo repo) {
        this.landmarkRepo = repo;
    }
    
    public GameSession(PlayerStateManager playerState, PuzzleController controller, LandmarkRepo landmarkRepo) {
        this.playerState = playerState;
        this.controller = controller;
        this.landmarkRepo = landmarkRepo;
    }

    public void loadPlayerState(Player player){
        this.playerState = new PlayerStateManager(player, false);
        this.controller = null;
        System.out.println("[INFO] Player Recentered.");
    }

    public void applySearchArea(double radiusMeters){
        this.controller = new PuzzleController(playerState.getPlayer(), this.landmarkRepo);
        this.controller.initTargetPool(radiusMeters);
        controller.startNewRound();
        System.out.println("[INFO] New game round started.");
    }

    public Landmark getCurrentTarget(){
        return controller.getCurrentTarget();
    }

    public boolean checkAnswerCorrect() {
        // Future integration: answer evaluator + epistemic 
        return controller.evaluateCurrentTarget();  // default true for mvp
    }
    
    public Landmark submitAndNext() {
        Landmark next = controller.submitCurrentAnswer();
    
        if (controller.isGameFinish()) {
            playerState.setGameFinished();  
        }
    
        return next;  
    }

    public void restartGame() {
        this.controller.startNewRound();
        System.out.println("[INFO] Game reset.");
        
    }

    public boolean isFinished() {
        return this.controller.isGameFinish();
    }
    
    public Set<Integer> getSolvedLandmarkIds() {
        return playerState.getPlayer().getSolvedLandmarkIDs();
    }

    public PlayerStateManager getPlayerState() {
        return this.playerState;
    }
}
