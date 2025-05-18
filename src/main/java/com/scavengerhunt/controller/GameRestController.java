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
import com.scavengerhunt.repository.RiddleRepository;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private Map<String, GameSession> sessionMap = new HashMap<>();
    private final GameDataRepository gameDataRepo;

    @Autowired
    private RiddleRepository riddleRepo;
    private PuzzleManager puzzleManager;

    public GameRestController(GameDataRepository gameDataRepo) {
        this.gameDataRepo = gameDataRepo;
    }

    @PostMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@RequestBody PlayerPositionRequest request) {
        
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) {
            Player player = new Player(request.getLatitude(), request.getLongitude(), request.getAngle());
            PlayerStateManager playerState = new PlayerStateManager(player, false);
            LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo);

            String userId = gameDataRepo.getUserRepo().findById(request.getUserId())
                .map(user -> user.getPlayerId())
                .orElse(null);

            session = new GameSession(playerState, landmarkManager, userId);
            sessionMap.put(userId, session);
        }
        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        return ResponseEntity.ok("[Backend] Player Position Updated.");
    }

    @PostMapping("/start-round")
    public ResponseEntity<?> startNewRound(@RequestBody StartRoundRequest request) {
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) return ResponseEntity.status(404).body("[Backend] Session Not Found.");

        if (session.getUserId() == null) {
            return ResponseEntity.status(403).body("[Backend] Must be logged in to start round.");
        }

        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        session.startNewRound(request.getRadiusMeters());

        Landmark target = session.getCurrentTarget();
        if (target == null) return ResponseEntity.status(404).body("[Backend] No target available.");

        Map<String, Object> targetInfo = new HashMap<>();
        targetInfo.put("name", target.getName());
        targetInfo.put("riddle", target.getRiddle());
        targetInfo.put("latitude", target.getLatitude());
        targetInfo.put("longitude", target.getLongitude());

        return ResponseEntity.ok(targetInfo);
    }


    @GetMapping("/first-target")
    public ResponseEntity<?> getNextTarget(@RequestParam String userId) {
        GameSession session = sessionMap.get(userId);
        if (session == null) return ResponseEntity.status(404).body("[Backend] Session Not Found.");
    
        Landmark target = session.getCurrentTarget();
    
        if (target == null) {
            target = session.selectNextTarget(); 
        }
    
        if (target == null) {
            return ResponseEntity.status(404).body("[Backend] No target available. Game may have finished.");
        }
    
        return ResponseEntity.ok(serializeLandmark(target));
    }
    

    @PostMapping("/submit-answer") // update user solved landmarks
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        GameSession session = sessionMap.get(userId);
        if (session == null) return ResponseEntity.status(404).body("Session not found");
        
        // ==== First Submission ====
        if (session.getCurrentTarget() == null) {
            Landmark first = session.selectNextTarget();
            if (first == null) return ResponseEntity.status(404).body("No target available in this region.");

            return ResponseEntity.ok(serializeLandmark(first));
        }

        // ==== Normal Submission ====
        boolean result = session.submitCurrentAnswer();
        if (!result){
            return ResponseEntity.status(404).body("Answer Incorrect.");
        }

        Landmark next = session.selectNextTarget();
        if (next == null){
            gameDataRepo.savePlayerProgress(session.getPlayerState());
            return ResponseEntity.ok("All riddles solved!");
        } else {
            return ResponseEntity.ok(serializeLandmark(next));
        }
    }

    @PostConstruct
    public void init() {
        this.puzzleManager = new PuzzleManager(riddleRepo);
    }


    private Map<String, Object> serializeLandmark(Landmark lm){
        Map<String, Object> targetInfo = new HashMap<>();
        targetInfo.put("name", lm.getName());
        targetInfo.put("riddle", puzzleManager.getRiddleForLandmark(lm.getId()));  
        targetInfo.put("latitude", lm.getLatitude());
        targetInfo.put("longitude", lm.getLongitude());
        return targetInfo;
    }
    
}



