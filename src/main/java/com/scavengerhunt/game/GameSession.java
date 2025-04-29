package com.scavengerhunt.game;

import java.util.Set;

public class GameSession {
    
    private PlayerStateManager playerState;
    private PuzzleController controller;
    private LandmarkManager landmarkManager;
    
    public GameSession(){}

    public GameSession(PlayerStateManager playerState, PuzzleController controller, LandmarkManager landmarkManager) {
        this.playerState = playerState;
        this.controller = controller;
        this.landmarkManager = landmarkManager;
    }

    public void loadPlayerState(Player player){
        this.playerState = new PlayerStateManager(player, false);
        this.controller = null;
        //TODO 
        System.out.println("[INFO] Player Recentered.");
    }

    public void startNewRound(){}

    public void applySearchArea(double radiusMeters){
        this.controller = new PuzzleController(playerState.getPlayer(), this.landmarkManager);
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
