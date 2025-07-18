package com.scavengerhunt.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.dto.LandmarkDTO;
import com.scavengerhunt.dto.PlayerPositionRequest;
import com.scavengerhunt.dto.StartRoundRequest;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.game.PuzzleManager;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.GeoUtils;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private Map<String, GameSession> sessionMap = new HashMap<>();

    @Autowired
    private GameDataRepository gameDataRepo;

    @Autowired
    private PuzzleManager puzzleManager;
    
    // EloCalculator is created dynamically in GameSession, not as a Spring bean

    @PostMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@RequestBody PlayerPositionRequest request) {
        
        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.ok("[Backend][API] Guest position updated (no session created).");
        }
        
        // create session for whatever user type
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) {
            Player player = new Player(request.getLatitude(), request.getLongitude(), request.getAngle(), request.getSpanDeg(), request.getConeRadiusMeters());
            LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, player.getCity());
            PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);

            String userId = request.getUserId();
            
            session = new GameSession(userId, gameDataRepo, playerState, landmarkManager, puzzleManager, 30);
            sessionMap.put(userId, session);
        }
        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());

        
        // user based response
        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.ok("[Backend][API] Guest position updated (session created).");
        } else {
            return ResponseEntity.ok("[Backend][API] Player Position Updated.");
        }
    }

    @PostMapping("/init-game")
    public ResponseEntity<?> initGame(@RequestBody PlayerPositionRequest request) {
        
        System.out.println("[InitGame] Request from user: " + request.getUserId());
        System.out.println("[InitGame] city: " + request.getCity());

        Player player = new Player(
            request.getLatitude(),
            request.getLongitude(),
            request.getAngle(),
            request.getSpanDeg(),
            request.getConeRadiusMeters()
        );

        LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, player.getCity());
        PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);

        String userId = request.getUserId();
        GameSession session = new GameSession(userId, gameDataRepo, playerState, landmarkManager, puzzleManager, 30);
        sessionMap.put(userId, session);
        //responding landmark coord for frontend rendering
        
        
        // List<Landmark> landmarks= gameDataRepo.getLandmarkRepo().findByCity(request.getCity());
        // Map<String, Object> response = new HashMap<>();
        // List<Map<String, Object>> frontendLandmarks = new ArrayList<>();

        // for (Landmark lm : landmarks) {
        //     Map<String, Object> oneLandmark = new HashMap<>();
        //     oneLandmark.put("name", lm.getName());
        //     oneLandmark.put("id", lm.getId());

        //     List<List<Double>> coords = new ArrayList<>();
        //     Coordinate[] polygon = GeoUtils.convertToJtsPolygon(lm.getGeometry()).getCoordinates();
        //     for (Coordinate coord : polygon) {
        //         coords.add(Arrays.asList(coord.getY(), coord.getX()));  // [lat, lng]
        //     }

        //     oneLandmark.put("coordinates", coords);
        //     frontendLandmarks.add(oneLandmark);
        // }

        // response.put("landmarks", frontendLandmarks);
        // return ResponseEntity.ok(response);

        // switch to using encapsulated landmarkDTO
        List<Landmark> landmarks = gameDataRepo.getLandmarkRepo().findByCity(request.getCity());
        List<LandmarkDTO> frontendLandmarks = new ArrayList<>();

        for (Landmark lm : landmarks) {
            List<List<Double>> coords = new ArrayList<>();
            Coordinate[] polygon = GeoUtils.convertToJtsPolygon(lm.getGeometry()).getCoordinates();
            for (Coordinate coord : polygon) {
                coords.add(Arrays.asList(coord.getY(), coord.getX()));  // [lat, lng]
            }

            LandmarkDTO dto = new LandmarkDTO(lm.getId(), lm.getName(), coords);
            frontendLandmarks.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("landmarks", frontendLandmarks);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start-round")
    public ResponseEntity<?> startNewRound(@RequestBody StartRoundRequest request) {

        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.status(403).body("[Backend][API] Must be logged in to start round.");
        }
        
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) return ResponseEntity.status(404).body("[Backend][API] Session Not Found.");

        if (session.getUserId() == null) {
            return ResponseEntity.status(403).body("[Backend][API] Must be logged in to start round.");
        }

        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        session.startNewRound(request.getRadiusMeters());

        Map<String, Object> currentTarget = session.getCurrentTarget();

        if (currentTarget == null) return ResponseEntity.status(404).body("[Backend][API] No target available.");

        return ResponseEntity.ok(currentTarget);
    }

    // @PostMapping("/submit-answer") // update user solved landmarks
    // public ResponseEntity<?> submitAnswer(@RequestBody Map<String, String> request) {
    //     String userId = request.get("userId");
    //     GameSession session = sessionMap.get(userId);
    //     if (session == null) return ResponseEntity.status(404).body("Session not found");
        
    //     boolean isCorrect = session.submitCurrentAnswer();
        
    //     if (isCorrect) {
    //         // Check if game is finished
    //         if (session.isGameFinished()) {
    //             return ResponseEntity.ok("Congratulations! You've completed all targets in this round.");
    //         } else {
    //             return ResponseEntity.ok("Answer correct! Next target selected.");
    //         }
    //     } else {
    //         // Check if game is finished due to too many wrong answers
    //         if (session.isGameFinished()) {
    //             return ResponseEntity.ok("Game over. You've exhausted all attempts for the available targets.");
    //         } else {
    //             return ResponseEntity.ok("Answer incorrect. Try again.");
    //         }
    //     }        
    // }

    // @GetMapping("/get-current-target")
    // public ResponseEntity<?> getCurrentTarget(@RequestParam String userId) {
    //     GameSession session = sessionMap.get(userId);
    //     if (session == null) return ResponseEntity.status(404).body("Session not found");
        
    //     Landmark target = session.getCurrentTarget();
    //     if (target == null) return ResponseEntity.status(404).body("No target available");
        
    //     return ResponseEntity.ok(target);
    // }
}


