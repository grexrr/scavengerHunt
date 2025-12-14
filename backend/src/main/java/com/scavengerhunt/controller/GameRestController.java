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
import com.scavengerhunt.dto.SubmitAnswerRequest;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.service.GameSessionService;
import com.scavengerhunt.utils.GeoUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/game")
@Tag(name="GameController", description="Game Controller APIs")
public class GameRestController {

    @Autowired
    private GameSessionService gameSessionService; 

    @Autowired
    private GameDataRepository gameDataRepo;

    @Autowired
    private com.scavengerhunt.client.PuzzleAgentClient puzzleAgentClient;

    @Autowired
    private com.scavengerhunt.client.LandmarkProcessorClient landmarkProcessorClient;
    
    // EloCalculator is created dynamically in GameSession, not as a Spring bean
    @Operation(
        summary = "Update player movement.",
        description = "Update a player's geological location."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Player position updated")
    })
    @PostMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@RequestBody PlayerPositionRequest request) {
        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.ok("[Backend][API] Guest position updated (no session created).");
        }
        
        // create session for whatever user type
        GameSession session = gameSessionService.getSession(request.getUserId());
        if (session == null) {
            double latitude = request.getLatitude();
            double longitude = request.getLongitude();
            String city = gameDataRepo.initLandmarkDataFromPosition(latitude, longitude);

            Player player = new Player(latitude, longitude, request.getAngle(), city, request.getSpanDeg(), request.getConeRadiusMeters());
            LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, landmarkProcessorClient, player.getCity());
            PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);
            com.scavengerhunt.game.PuzzleManager puzzleManager = new com.scavengerhunt.game.PuzzleManager(gameDataRepo, puzzleAgentClient);

            String userId = request.getUserId();
            
            session = new GameSession(userId, gameDataRepo, playerState, landmarkManager, puzzleManager, 30);
            gameSessionService.putSession(userId, session);
        }
        session.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());

        
        // user based response
        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.ok("[Backend][API] Guest position updated (session created).");
        } else {
            return ResponseEntity.ok("[Backend][API] Player Position Updated.");
        }
    }

    @Operation(
        summary = "Initialize game session",
        description = "Initialize a new game session or update existing session with landmarks for the player's location."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game initialized successfully, landmarks returned")
    })
    @PostMapping("/init-game")
    public synchronized ResponseEntity<?> initGame(@RequestBody PlayerPositionRequest request) {
        
        System.out.println("[InitGame] Request from user: " + request.getUserId());
        // System.out.println("[InitGame] city: " + request.getCity());

        String userId = request.getUserId();
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        String city = gameDataRepo.initLandmarkDataFromPosition(lat, lng);
        
        // Check if there's already an active session for this user
        GameSession existingSession = gameSessionService.getSession(userId);
        if (existingSession != null && !existingSession.isGameFinished()) {
            System.out.println("[InitGame] Active session exists for user " + userId + ", updating position only");
            existingSession.updatePlayerPosition(request.getLatitude(), request.getLongitude(), request.getAngle());
            
            // Still return landmarks for consistency
            List<Landmark> landmarks = gameDataRepo.getLandmarkRepo().findByCity(city);
            List<LandmarkDTO> frontendLandmarks = new ArrayList<>();

            for (Landmark lm : landmarks) {
                // polygon
                List<List<Double>> coords = new ArrayList<>();
                Coordinate[] polygon = GeoUtils.convertToJtsPolygon(lm.getGeometry()).getCoordinates();
                for (Coordinate coord : polygon) {
                    coords.add(Arrays.asList(coord.getY(), coord.getX()));  // [lat, lng]
                }
                LandmarkDTO dto = new LandmarkDTO(lm.getId(), lm.getName(), lm.getCentroid(), coords);
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
            city,
            request.getSpanDeg(),
            request.getConeRadiusMeters()
        );

        LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo, landmarkProcessorClient, player.getCity());
        PlayerStateManager playerState = new PlayerStateManager(player, landmarkManager, gameDataRepo);
        com.scavengerhunt.game.PuzzleManager puzzleManager = new com.scavengerhunt.game.PuzzleManager(gameDataRepo, puzzleAgentClient);

        GameSession session = new GameSession(userId, gameDataRepo, playerState, landmarkManager, puzzleManager, 30);
        gameSessionService.putSession(userId, session);
        
        List<Landmark> landmarks = gameDataRepo.getLandmarkRepo().findByCity(city);
        List<LandmarkDTO> frontendLandmarks = new ArrayList<>();

        for (Landmark lm : landmarks) {
            List<List<Double>> coords = new ArrayList<>();
            Coordinate[] polygon = GeoUtils.convertToJtsPolygon(lm.getGeometry()).getCoordinates();
            for (Coordinate coord : polygon) {
                coords.add(Arrays.asList(coord.getY(), coord.getX()));  // [lat, lng]
            }

            LandmarkDTO dto = new LandmarkDTO(lm.getId(), lm.getName(), lm.getCentroid(), coords);
            frontendLandmarks.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("landmarks", frontendLandmarks);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Start a new round",
        description = "Validate the player session, ensure the game is not finished, then start a new round and return the current target."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Round started, target returned"),
        @ApiResponse(responseCode = "400", description = "Game already finished or invalid request"),
        @ApiResponse(responseCode = "403", description = "User must be authenticated to start a round"),
        @ApiResponse(responseCode = "404", description = "Session or target not found")
    })
    @PostMapping("/start-round")
    public synchronized ResponseEntity<?> startNewRound(@RequestBody StartRoundRequest request) {

        if (request.getUserId().startsWith("guest-")) {
            return ResponseEntity.status(403).body("[Backend][API] Must be logged in to start round.");
        }
        
        GameSession session = gameSessionService.getSession(request.getUserId());
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

    @Operation(
        summary = "Submit an answer for the current target",
        description = "Submit the player's answer, update timing info, and return correctness plus next target data."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Answer processed successfully"),
        @ApiResponse(responseCode = "404", description = "Active session not found for user")
    })
    @PostMapping("/submit-answer")
    public synchronized ResponseEntity<?> submitAnswer(@RequestBody SubmitAnswerRequest request) {
        String userId = request.getUserId();
        System.out.println("[Debug] Submit answer request from user: " + userId);
        
        GameSession session = gameSessionService.getSession(userId);
        if (session == null) {
            System.out.println("[Error] Session not found for user: " + userId);
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", "Session not found"
            ));
        }

        long secondsUsed = request.getSecondsUsed();
        System.out.println("[Debug] Seconds used: " + secondsUsed);

        // Update player position with current angle if provided
        if (request.getCurrentAngle() != null && request.getLatitude() != null && request.getLongitude() != null) {
            session.updatePlayerPosition(
                request.getLatitude(),
                request.getLongitude(),
                request.getCurrentAngle()
            );
            System.out.println("[Debug] Updated player position with current angle: " + request.getCurrentAngle());
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
            response.put("message", "Incorrect. Try a dgain or check your position.");
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

    @Operation(
        summary = "Finish the current round",
        description = "Clear the player's session and mark the game as finished."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session cleared successfully"),
        @ApiResponse(responseCode = "400", description = "userId missing from request")
    })
    @PostMapping("/finish-round")
    public ResponseEntity<?> finishRound(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing userId.");
        }

        gameSessionService.removeSession(userId);  
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Game session ended.");

        return ResponseEntity.ok(res);
    }
}


