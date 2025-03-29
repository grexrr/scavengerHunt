package com.scavengerhunt;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkRepo;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PuzzleController;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("== Scavenger Hunt Test ==");
        // game.initGame(51.8954396, -8.4890143, 0); //init player, 
        // 1. 初始化玩家与题库管理器
        Player player = new Player(51.8954396, -8.4890143, 0);
        player.setPlayerId("player001");
        player.setNickname("TestUser");

        LandmarkRepo landmarkRepo = new LandmarkRepo();
        GameDataRepo dataRepo = new GameDataRepo();
        landmarkRepo.loadLandmarks(dataRepo); // 加载本轮题库

        // 2. 创建控制器
        PuzzleController controller = new PuzzleController(player, landmarkRepo);

        // 3. 启动新一轮
        controller.startNewRound();
        System.out.println("[INFO] New game round started.");

        // 4. 获取一个目标（测试 getNextTarget 流程）
        Landmark nextTarget = controller.getNextTarget();
        System.out.println("Next target: " + nextTarget.getName());

        // 5. 模拟“玩家解出这个 landmark”
        landmarkRepo.markSolved(nextTarget);
        player.updatePlayerSolvedLandmark(nextTarget);
        System.out.println("[INFO] Solved landmark: " + nextTarget.getName());

        // 6. 判断游戏是否结束（目前只有两个 landmark 所以解一个还没结束）
        boolean finished = controller.isGameFinish();
        System.out.println("Is game finished? " + finished);

        // 7. 模拟继续解一个 landmark
        Landmark nextTarget2 = controller.getNextTarget();
        System.out.println("Next target: " + nextTarget2.getName());

        landmarkRepo.markSolved(nextTarget2);
        player.updatePlayerSolvedLandmark(nextTarget2);
        System.out.println("[INFO] Solved landmark: " + nextTarget2.getName());

        // 8. 再次检查游戏是否结束
        finished = controller.isGameFinish();
        System.out.println("Is game finished? " + finished);

        // 9. 模拟上传答题进度
        // dataRepo.savePlayerProgress(controller.getPlayerStateManager());

        System.out.println("== Test Complete ==");
    }
}
