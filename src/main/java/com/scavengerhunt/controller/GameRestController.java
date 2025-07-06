package com.scavengerhunt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.dto.PlayerPositionRequest;
import com.scavengerhunt.dto.StartRoundRequest;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.game.PuzzleManager;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.EloUtils;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private Map<String, GameSession> sessionMap = new HashMap<>();

    @Autowired
    private GameDataRepository gameDataRepo;

    @Autowired
    private PuzzleManager puzzleManager;
    
    @Autowired
    private EloUtils eloUtils;


    @PostMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@RequestBody PlayerPositionRequest request) {
        
        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.ok("[Backend] Guest position updated (no session created).");
        }
        
        // create session for login use
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) {
            Player player = new Player(request.getLatitude(), request.getLongitude(), request.getAngle());
            LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, player.getCity());
            PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);

            String userId = request.getUserId();
            
            session = new GameSession(userId, gameDataRepo, playerState, landmarkManager, puzzleManager, eloUtils);
            sessionMap.put(userId, session);
        }
        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        
        // user based response
        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.ok("[Backend] Guest position updated (session created).");
        } else {
            return ResponseEntity.ok("[Backend] Player Position Updated.");
        }
    }

    @PostMapping("/init-game")
    public void initGame(@RequestBody PlayerPositionRequest request) {
     
        Player player = new Player(request.getLatitude(), request.getLongitude(), request.getAngle());
        LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, player.getCity());
        PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);

        String userId = request.getUserId();
        
        GameSession session = new GameSession(userId, gameDataRepo, playerState, landmarkManager, puzzleManager, eloUtils);
        sessionMap.put(userId, session);
    }

    @PostMapping("/start-round")
    public ResponseEntity<?> startNewRound(@RequestBody StartRoundRequest request) {

        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.status(403).body("[Backend] Must be logged in to start round.");
        }
        
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) return ResponseEntity.status(404).body("[Backend] Session Not Found.");

        if (session.getUserId() == null) {
            return ResponseEntity.status(403).body("[Backend] Must be logged in to start round.");
        }

        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        session.startNewRound(request.getRadiusMeters());

        Landmark target = session.getCurrentTarget();
        if (target == null) return ResponseEntity.status(404).body("[Backend] No target available.");

        return ResponseEntity.ok(target);
    }

    @PostMapping("/submit-answer") // update user solved landmarks
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        GameSession session = sessionMap.get(userId);
        if (session == null) return ResponseEntity.status(404).body("Session not found");
        
        boolean isCorrect = session.submitCurrentAnswer();
        
        if (isCorrect) {
            // Check if game is finished
            if (session.isGameFinished()) {
                return ResponseEntity.ok("Congratulations! You've completed all targets in this round.");
            } else {
                return ResponseEntity.ok("Answer correct! Next target selected.");
            }
        } else {
            // Check if game is finished due to too many wrong answers
            if (session.isGameFinished()) {
                return ResponseEntity.ok("Game over. You've exhausted all attempts for the available targets.");
            } else {
                return ResponseEntity.ok("Answer incorrect. Try again.");
            }
        }        
    }

    @GetMapping("/get-current-target")
    public ResponseEntity<?> getCurrentTarget(@RequestParam String userId) {
        GameSession session = sessionMap.get(userId);
        if (session == null) return ResponseEntity.status(404).body("Session not found");
        
        Landmark target = session.getCurrentTarget();
        if (target == null) return ResponseEntity.status(404).body("No target available");
        
        return ResponseEntity.ok(target);
    }
}


