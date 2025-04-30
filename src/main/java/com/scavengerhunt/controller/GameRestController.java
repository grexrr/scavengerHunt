package com.scavengerhunt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.dto.PlayerPositionRequest;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;



@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private Map<String, GameSession> sessionMap = new HashMap<>();
    private final GameDataRepository gameDataRepo;

    public GameRestController(GameDataRepository gameDataRepo) {
        this.gameDataRepo = gameDataRepo;
    }

    @PostMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@RequestBody PlayerPositionRequest request) {
        String userId = request.getPlayerId();
        GameSession session = sessionMap.get(userId);
        if (session == null) {
            Player player = new Player(request.getLatitude(), request.getLongitude(), request.getAngle());
            PlayerStateManager playerState = new PlayerStateManager(player, false);
            LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo);
            session = new GameSession(playerState, landmarkManager);
            sessionMap.put(userId, session);
        }
        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        return ResponseEntity.ok("Player Position Updated.");
    }
    

    @PostMapping("/submit-answer") // update user solved landmarks
    public void submitAnswer() {
       
    }

}

