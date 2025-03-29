package com.scavengerhunt;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkRepo;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PuzzleController;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("== Scavenger Hunt Test ==");
        
        // 1. Init Repos
        // game.initGame(51.8954396, -8.4890143, 0); //init player, 
        Player player = new Player(51.8954396, -8.4890143, 0);
        player.setPlayerId("player001");
        player.setNickname("TestUser");

        LandmarkRepo landmarkRepo = new LandmarkRepo();
        GameDataRepo dataRepo = new GameDataRepo();
        landmarkRepo.loadLandmarks(dataRepo); // Load landmarks for this round

        // 2. Create controller
        PuzzleController controller = new PuzzleController(player, landmarkRepo);

        // 3. Start a new round
        controller.startNewRound();
        System.out.println("[INFO] New game round started.");

        // 4. Get a target (testing getNextTarget flow)
        Landmark nextTarget = controller.getNextTarget();
        System.out.println("Next target: " + nextTarget.getName());

        // 5. Simulate "player solves this landmark"
        landmarkRepo.markSolved(nextTarget);
        player.updatePlayerSolvedLandmark(nextTarget);
        System.out.println("[INFO] Solved landmark: " + nextTarget.getName());

        // 6. Check if the game is finished (currently only two landmarks so solving one doesn't end the game)
        boolean finished = controller.isGameFinish();
        System.out.println("Is game finished? " + finished);

        // 7. Simulate solving another landmark
        Landmark nextTarget2 = controller.getNextTarget();
        System.out.println("Next target: " + nextTarget2.getName());

        landmarkRepo.markSolved(nextTarget2);
        player.updatePlayerSolvedLandmark(nextTarget2);
        System.out.println("[INFO] Solved landmark: " + nextTarget2.getName());

        // 8. Check again if the game is finished
        finished = controller.isGameFinish();
        System.out.println("Is game finished? " + finished);

        // 9. Simulate uploading progress
        // dataRepo.savePlayerProgress(controller.getPlayerStateManager());

        System.out.println("== Test Complete ==");
    }
}
