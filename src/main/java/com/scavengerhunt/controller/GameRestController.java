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
    public synchronized ResponseEntity<?> initGame(@RequestBody PlayerPositionRequest request) {
        
        System.out.println("[InitGame] Request from user: " + request.getUserId());
        System.out.println("[InitGame] city: " + request.getCity());

        String userId = request.getUserId();
        
        // Check if there's already an active session for this user
        GameSession existingSession = sessionMap.get(userId);
        if (existingSession != null && !existingSession.isGameFinished()) {
            System.out.println("[InitGame] Active session exists for user " + userId + ", updating position only");
            existingSession.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
            
            // Still return landmarks for consistency
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

        // Create new session only if no active session exists
        System.out.println("[InitGame] Creating new session for user: " + userId);
        Player player = new Player(
            request.getLatitude(),
            request.getLongitude(),
            request.getAngle(),
            request.getSpanDeg(),
            request.getConeRadiusMeters()
        );

        LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, player.getCity());
        PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);

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
    public synchronized ResponseEntity<?> startNewRound(@RequestBody StartRoundRequest request) {

        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.status(403).body("[Backend][API] Must be logged in to start round.");
        }
        
        GameSession session = sessionMap.get(request.getUserId());
        if (session == null) return ResponseEntity.status(404).body("[Backend][API] Session Not Found.");

        if (session.getUserId() == null) {
            return ResponseEntity.status(403).body("[Backend][API] Must be logged in to start round.");
        }
        
        // Check if game is already finished to prevent starting new round on finished session
        if (session.isGameFinished()) {
            System.out.println("[StartRound] Cannot start round - game already finished for user: " + request.getUserId());
            return ResponseEntity.status(400).body("[Backend][API] Game already finished. Please initialize a new game.");
        }

        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
        session.startNewRound(request.getRadiusMeters());

        Map<String, Object> currentTarget = session.getCurrentTarget();

        if (currentTarget == null) return ResponseEntity.status(404).body("[Backend][API] No target available.");

        return ResponseEntity.ok(currentTarget);
    }

    @PostMapping("/submit-answer")
    public synchronized ResponseEntity<?> submitAnswer(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        System.out.println("[Debug] Submit answer request from user: " + userId);
        
        GameSession session = sessionMap.get(userId);
        if (session == null) {
            System.out.println("[Error] Session not found for user: " + userId);
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", "Session not found"
            ));
        }

        long secondsUsed = Long.parseLong(request.get("secondsUsed")); 
        System.out.println("[Debug] Seconds used: " + secondsUsed);

        // Update player position with current angle if provided
        String currentAngleStr = request.get("currentAngle");
        String latitudeStr = request.get("latitude");
        String longitudeStr = request.get("longitude");
        
        if (currentAngleStr != null && latitudeStr != null && longitudeStr != null) {
            double currentAngle = Double.parseDouble(currentAngleStr);
            double latitude = Double.parseDouble(latitudeStr);
            double longitude = Double.parseDouble(longitudeStr);
            session.updatePlayerPosition(latitude, longitude, currentAngle);
            System.out.println("[Debug] Updated player position with current angle: " + currentAngle);
        }

        boolean isCorrect = session.submitCurrentAnswer(secondsUsed);
        boolean gameFinished = session.isGameFinished();
        
        System.out.println("[Debug] Submit answer result: isCorrect=" + isCorrect + ", gameFinished=" + gameFinished);

        Map<String, Object> response = new HashMap<>();
        response.put("isCorrect", isCorrect);
        response.put("gameFinished", gameFinished);

        // message
        if (gameFinished && isCorrect) {
            System.out.println("[Debug] Game finished with correct answer - success message");
            response.put("message", "Congratulations! You've completed all targets in this round.");
        } else if (gameFinished) {
            System.out.println("[Debug] Game finished with incorrect answer - failure message");
            response.put("message", "Game over. You've exhausted all attempts for the available targets.");
        } else if (isCorrect) {
            System.out.println("[Debug] Correct answer, game continues");
            response.put("message", "Correct! Next target selected.");
        } else {
            System.out.println("[Debug] Incorrect answer, game continues");
            response.put("message", "Incorrect. Try again or check your position.");
        }

        // target info: currentTarget or nextTarget
        if (!gameFinished) {
            Map<String, Object> target = session.getCurrentTarget();
            if (target != null) {
                response.put("target", target);
            }
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish-round")
    public ResponseEntity<?> finishRound(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing userId.");
        }

        sessionMap.remove(userId);  
        System.out.println("[Backend][API] Session cleared for user: " + userId);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Game session ended.");

        return ResponseEntity.ok(res);
    }

    // @GetMapping("/get-current-target")
    // public ResponseEntity<?> getCurrentTarget(@RequestParam String userId) {
    //     GameSession session = sessionMap.get(userId);
    //     if (session == null) return ResponseEntity.status(404).body("Session not found");
        
    //     Landmark target = session.getCurrentTarget();
    //     if (target == null) return ResponseEntity.status(404).body("No target available");
        
    //     return ResponseEntity.ok(target);
    // }
}


