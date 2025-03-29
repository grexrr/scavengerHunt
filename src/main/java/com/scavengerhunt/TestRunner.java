package com.scavengerhunt;

import java.util.List;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PlayerStateManager;
public class TestRunner {
    
    public static void main(String[] args) {
    
        // GameUIController game = new GameUIController();
        
        // game.initGame(51.8954396, -8.4890143, 0); //init player, 
        // game.applySearchArea(5000.0);
        

        GameDataRepo dataManager = new GameDataRepo();
        List<Landmark> loadedLandmarks = dataManager.loadLandmarks();
       
        Player player = new Player(51.8954396, -8.4890143, 0); // Init Player Location at UCC main gate
        player.setPlayerId("p001");
        PlayerStateManager playerState = new PlayerStateManager(player, null);

        LandmarkManager landmarkManager = new LandmarkManager(loadedLandmarks);
        List<Landmark> currentRoundLandmarks = landmarkManager.filterLocalInRadius(playerState, 1000); // Circling
        // Possible Landmark Unfound Need to be handle!!!

        LandmarkManager roundManager = new LandmarkManager(currentRoundLandmarks);
        roundManager.updateUnsolved(playerState);


        Landmark first = roundManager.selectNearestToPlayer(playerState);
        playerState.updateCurrentTarget(first);
        System.out.println("!!!!First Riddle: " + first.getName());
        playerState.markLandmarkSolved(first);
        roundManager.updateUnsolved(playerState);

        // 继续选下一个谜题
        Landmark second = roundManager.selectNextLandmark(first);
        if (second != null) {
            System.out.println("!!!!Second Riddle:" + second.getName());

            playerState.updateCurrentTarget(second);
            playerState.markLandmarkSolved(second);
            roundManager.updateUnsolved(playerState);

            Landmark third = roundManager.selectNextLandmark(second);
            if (third == null) {
                System.out.println("!!!!Congrats!!!!! All Riddle Solved!!!!!");
                playerState.finishGame();
            }

        } else {
            System.out.println("!!!!Congrats!!!!! All Riddle Solved!!!!!");
            playerState.finishGame();
        }


        dataManager.savePlayerProgress(player);
    }
}
