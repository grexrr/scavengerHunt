// game.initGame(51.8954396, -8.4890143, 0); //init player, 

package com.scavengerhunt;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkRepo;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PlayerStateManager;


public class TestRunner {

    public static void main(String[] args) {
        System.out.println("== Scavenger Hunt Test via GameSession ==");

        // Step 1: Load landmarks via GameDataRepo and LandmarkRepo
        GameDataRepo dataRepo = new GameDataRepo();
        LandmarkRepo landmarkRepo = new LandmarkRepo(dataRepo);
        landmarkRepo.loadLandmarks();

        // Step 2: Create a player and GameSession
        Player player = new Player(51.895506, -8.488848, 0); // UCC Main Gate
        player.setPlayerId("p001");
        PlayerStateManager stateManager = new PlayerStateManager(player, false);

        GameSession session = new GameSession(stateManager, null, landmarkRepo);

        // Step 3: Start a new round with 500m radius
        session.startNewRound(500);

        // Step 4: Get current target
        Landmark target = session.getCurrentTarget();
        System.out.println("[TARGET] " + target.getName());

        // Step 5: Simulate solving puzzles until game is finished
        while (!session.isFinished()) {
            boolean correct = session.checkAnswerCorrect();
            if (correct) {
                Landmark next = session.submitAndNext();
                if (next != null) {
                    System.out.println("[NEXT] " + next.getName());
                } else {
                    System.out.println("[COMPLETE] All riddles solved.");
                }
            } else {
                System.out.println("[ERROR] Incorrect answer (not implemented in MVP)");
                break;
            }
        }

        // Final solved landmark IDs
        System.out.println("[SOLVED IDS] " + session.getSolvedLandmarkIds());
        System.out.println("== Test Complete ==");
    }
}