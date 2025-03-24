package com.scavengerhunt;

import java.util.List;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PlayerStateManager;

public class TestRunner {
    
    public static void main(String[] args) {
    
        GameDataRepo dataManager = new GameDataRepo();
        List<Landmark> loadedLandmarks = dataManager.loadLandmarks();

        Player player = new Player(51.8930, -8.4940, 0); // 玩家位置
        player.setPlayerId("p001");
        PlayerStateManager playerState = new PlayerStateManager(player, null);

        LandmarkManager landmarkManager = new LandmarkManager(loadedLandmarks);
        List<Landmark> filtered = landmarkManager.filterLocalInRadius(playerState, 50); // 模拟划圈

        LandmarkManager roundManager = new LandmarkManager(filtered);
        roundManager.updateUnsolved(playerState);


        Landmark first = roundManager.selectNearestToPlayer(playerState);
        playerState.updateCurrentTarget(first);
        System.out.println("!!!!First Riddle: " + first.getName());

        playerState.markLandmarkSolved(first);
        roundManager.updateUnsolved(playerState);

        Landmark second = roundManager.selectNextLandmark(first);
        if (second != null) {
            System.out.println("!!!!Second Riddle:" + second.getName());
        } else {
            System.out.println("!!!!Congrats!!!!! All Riddle Solved!!!!!");
            playerState.finishGame();
        }

        dataManager.savePlayerProgress(player);
    }
}
