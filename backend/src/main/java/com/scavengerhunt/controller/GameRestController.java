package com.scavengerhunt.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.dto.LandmarkDTO;
import com.scavengerhunt.dto.PlayerPositionRequest;
import com.scavengerhunt.dto.StartRoundRequest;
import com.scavengerhunt.dto.SubmitAnswerRequest;
import com.scavengerhunt.game.GameLogicManager;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.repository.AnswerTransactionRecordRepository;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.service.GameSessionService;
import com.scavengerhunt.service.JobCoordinator;
import com.scavengerhunt.utils.GeoUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/game")
@Tag(name="GameController", description="Game Controller APIs")
public class GameRestController {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private GameDataRepository gameDataRepo;

    @Autowired
    private AnswerTransactionRecordRepository answerTransactionRecordRepo;

    @Autowired
    private PuzzleAgentClient puzzleAgentClient;

    @Autowired
    private LandmarkProcessorClient landmarkProcessorClient;

    @Autowired
    private JobCoordinator jobCoordinator;

    // EloCalculator is created dynamically in GameSession, not as a Spring bean
    @Operation(
        summary = "Update player movement.",
        description = "Update a player's geological location."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Player position updated")
    })
    @PostMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@Valid @RequestBody PlayerPositionRequest request) {

        String userId = currentUserId();

        double lat = request.getLatitude();
        double lng = request.getLongitude();
        double angle = request.getAngle();

        // session Game Session Management
        PersistedGameSession session = gameSessionService.findByUserId(userId).orElse(null);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("[Backend][updatePlayerPosition] No Session for player: " + userId);
        } else {
            session.setPlayerLat(lat);
            session.setPlayerLng(lng);
            session.setPlayerAngle(angle);
            session.setLastUpdated(Instant.now());
            gameSessionService.save(session);
        }
        // user based response
        return ResponseEntity.ok("[Backend][updatePlayerPosition] Player Position Updated.");
    }

    @Operation(
        summary = "Initialize game session",
        description = "Initialize a new game session or update existing session with landmarks for the player's location."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game initialized successfully, landmarks returned")
    })
    @PostMapping("/init-game")
    public ResponseEntity<?> initGame(@Valid @RequestBody PlayerPositionRequest request) {
        String userId = currentUserId();
        System.out.println("[InitGame] Request from user: " + userId);
        // System.out.println("[InitGame] city: " + request.getCity());

        double lat = request.getLatitude();
        double lng = request.getLongitude();
        double angle = request.getAngle();
        // String city = gameDataRepo.initLandmarkDataFromPosition(lat, lng);

        String city;
        try{
            city = landmarkProcessorClient.resolveCity(lat, lng);
        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body(
                Map.of("status", "error", "message", "Could not resolve city from coordinates")
            );
        }

        List<Landmark> existing = gameDataRepo.findByCity(city);
        if (existing.size() < 10) {
            jobCoordinator.enqueueFetchLandmarks(city, lat, lng);
            return ResponseEntity.accepted().body(
                Map.of(
                    "status", "PREPARING",
                    "message", "Landmarks are being prepared for " + city + ". Please try again in a few seconds.",
                    "city", city
                )
            );
        }

        List<LandmarkDTO> frontendLandmarks = new ArrayList<>();

        for (Landmark lm : existing) {
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

        // Check if there's already an active session for this user
        PersistedGameSession session = gameSessionService.findByUserId(userId).orElse(null);

        if (session == null || session.isFinished()) {
            System.out.println("[Backend][InitGame] Creating new session for user: " + userId);
            session = gameSessionService.createSession(userId, city);
        }

        // Update and save session position every time
        session.updatePlayerPosition(lat, lng, angle);
        gameSessionService.save(session);

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
    public ResponseEntity<?> startNewRound(@Valid @RequestBody StartRoundRequest request) {

        String userId = currentUserId();

        PersistedGameSession session = gameSessionService.findByUserId(userId).orElse(null);
        if (session == null) return ResponseEntity.status(404).body("[Backend][startNewRound] Session Not Found!");

        // Check if game is already finished to prevent starting new round on finished session
        if (session.isFinished()) {
            System.out.println("[Backend][startNewRound] Cannot start round - game already finished for user: " + userId);
            return ResponseEntity.status(400).body("[Backend][startNewRound] Game already finished. Please initialize a new game.");
        }

        // Run round logic
        GameLogicManager game = new GameLogicManager(session, gameDataRepo, landmarkProcessorClient, puzzleAgentClient, answerTransactionRecordRepo, 30);
        game.startNewRound(request.getRadiusMeters());

        Map<String, Object> currentTarget = game.getCurrentTarget();
        if (currentTarget == null) return ResponseEntity.status(404).body("[Backend][startNewRound] No target available.");

        // Session Update
        gameSessionService.save(session);

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
    public ResponseEntity<?> submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {

        String userId = currentUserId();

        PersistedGameSession session = gameSessionService.findByUserId(userId).orElse(null);
        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error", "message", "Session not found"
            ));
        }

        // Recounstruct GameSession
        double lat = request.getLatitude() != null ? request.getLatitude() : session.getPlayerLat();
        double lng = request.getLongitude() != null ? request.getLongitude() : session.getPlayerLng();
        double angle = request.getCurrentAngle() != null ? request.getCurrentAngle() : session.getPlayerAngle();
        GameLogicManager game = new GameLogicManager(session, gameDataRepo, landmarkProcessorClient, puzzleAgentClient, answerTransactionRecordRepo, 30);

        // Update player position with current angle if provided
        if (request.getCurrentAngle() != null && request.getLatitude() != null && request.getLongitude() != null) {
            game.updatePlayerPosition(lat, lng, angle);
        }

        boolean isCorrect = game.submitCurrentAnswer(request.getSecondsUsed());
        boolean gameFinished = game.isGameFinished();

        // Write results back to session
        gameSessionService.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("isCorrect", isCorrect);
        response.put("gameFinished", gameFinished);

        if (gameFinished && isCorrect) {
            response.put("message", "Congratulations! You've completed all targets in this round.");
        } else if (gameFinished) {
            response.put("message", "Game over. You've exhausted all attempts for the available targets.");
        } else if (isCorrect) {
            response.put("message", "Correct! Next target selected.");
        } else {
            response.put("message", "Incorrect. Try again or check your position.");
        }

        if (!gameFinished) {
            Map<String, Object> target = game.getCurrentTarget();
            if (target != null) response.put("target", target);
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
    public ResponseEntity<?> finishRound() {
        String userId = currentUserId();
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing userId.");
        }

        gameSessionService.removeSession(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Game session ended.");

        return ResponseEntity.ok(res);
    }

    private String currentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}


