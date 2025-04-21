package com.scavengerhunt.ui;

import org.springframework.stereotype.Component;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkRepo;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.game.PuzzleController;

@Component
public class UIController {
    
    private GameSession session;
    private GameDataRepo gameData;


    public UIController() {
    }

    /**
     * Core
     */

    public void initGame(double latitude, double longitude, double angle){
        Player player = new Player(latitude, longitude, angle);
        player.setPlayerNickname("default-player");

        setGameData();
        
        LandmarkRepo landmarkManager = new LandmarkRepo(getGameData());
        
        PlayerStateManager playerState = new PlayerStateManager(player, false);
        PuzzleController contoller = new PuzzleController(player, landmarkManager);

        session = new GameSession(playerState, contoller, landmarkManager);
        setSession(session);

        System.out.println("[UI] Game initialized");
    }

    public void startNewRound(double radius){
        session.applySearchArea(radius);
        Landmark currentTarget = session.getCurrentTarget();
        System.out.println("[UI] Current Target: " + currentTarget.getName()); // mvp testing
    }

    public Landmark submitAnswer(){

        Landmark next = session.submitAndNext();
        if (next == null) {
            System.out.println("[UI] All riddles solved!");
        } else {
            System.out.println("[UI] Next Target: " + next.getName());
        }
        return next;
        
    }
    
    /**
     * Getter & Setter
     */

    public GameSession getSession() {
        return session;
    }

    private void setSession(GameSession session) {
        this.session = session;
    }

    private GameDataRepo getGameData() {
        return gameData;
    }

    private void setGameData() {
        this.gameData = new GameDataRepo();
    }
}
