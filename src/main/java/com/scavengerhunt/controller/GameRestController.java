package com.scavengerhunt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.game.GameSession;
import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.game.LandmarkManager;
import com.scavengerhunt.game.Player;
import com.scavengerhunt.game.PlayerStateManager;
import com.scavengerhunt.game.PuzzleController;


@RestController
@RequestMapping("/api/game")
public class GameRestController {

    public GameSession session;
    private final GameDataRepo gameDataRepo = new GameDataRepo();

    @PostMapping("/init")
    public ResponseEntity<String> initGame(@RequestBody PlayerInitRequest request) {
        System.out.println("[DEBUG] initGame() called");

        Player player = new Player(request.getLatitude(), request.getLongitude(), request.getAngle());
        player.setPlayerNickname("default-player");
        
        System.out.println("[DEBUG] Coordination Received: " + request.getLatitude() + ", " + request.getLongitude());
        
        LandmarkManager landmarkManager = new LandmarkManager(gameDataRepo);
        PlayerStateManager playerState = new PlayerStateManager(player, false);
        PuzzleController controller = new PuzzleController(player, landmarkManager);
        
        this.session = new GameSession(playerState, controller, landmarkManager);
        System.out.println("[Game] Initialized session for player");

        return ResponseEntity.ok("Game Initialized");
    }

    @PostMapping("/start-round")
    public ResponseEntity<String> startRound(@RequestBody StartRoundRequest request) {
        System.out.println("[DEBUG] startRound called: " + request.getLatitude() + ", radius: " + request.getRadius());
        
        if (session == null) return ResponseEntity.badRequest().body("Session not initialized");
        // update player coord
        session.getPlayerState().updatePlayerPosition(
            request.getLatitude(), request.getLongitude(), request.getAngle()
        );

        // init riddle
        session.applySearchArea(request.getRadius());

        // Landmark target = session.getCurrentTarget();
        // System.out.println("[DEBUG] New target: " + (target != null ? target.getName() : "None"));
        return ResponseEntity.ok("Round started.");
    }
    
    @GetMapping("/target")
    public ResponseEntity<?> getCurrentTarget() {
        if (session == null) return ResponseEntity.badRequest().body("Session not initialized");
        
        Landmark target = session.getCurrentTarget();
        if (target == null) {
            return ResponseEntity.status(404).body("No target selected.");
        }

        System.out.println("[DEBUG] New target: " + (target != null ? target.getName() : "None"));    
        return ResponseEntity.ok(new TargetDTO(target));
    }

    @PutMapping("/update-position")
    public ResponseEntity<String> updatePlayerPosition(@RequestBody PlayerUpdateRequest request) {
        System.out.println("[DEBUG] update-position called with lat=" + request.getLatitude() + ", lng=" + request.getLongitude());
    
        if (session == null) {
            return ResponseEntity.badRequest().body("Game not initialized.");
        }
    
        session.getPlayerState().updatePlayerPosition(
            request.getLatitude(), request.getLongitude(), request.getAngle()
        );
    
        return ResponseEntity.ok("Player position updated.");
    }
    
    @PostMapping("/submit-answer")
    public ResponseEntity<String> submitAnswer() {
        if (session == null) return ResponseEntity.badRequest().body("Session not initialized");
        Landmark next = session.submitAndNext();
        if (next == null) {
            System.out.println("[Game] All riddles solved!");
            return ResponseEntity.ok("All riddles solved!");
        } else {
            System.out.println("[Game] Next Target: " + next.getName());
            return ResponseEntity.ok("Next target: " + next.getName());
        }
    }

    // DTO
    public static class PlayerInitRequest {
        private double latitude;
        private double longitude;
        private double angle;

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
    }

    public static class PlayerUpdateRequest {
        private double latitude;
        private double longitude;
        private double angle;

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
    }

    public static class StartRoundRequest {
        private double latitude;
        private double longitude;
        private double angle;
        private int radius;
    
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
    
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    
        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
    
        public int getRadius() { return radius; }
        public void setRadius(int radius) { this.radius = radius; }
    }

    public static class TargetDTO {
        private String name;
        private String riddle;
        private double latitude;
        private double longitude;
    
        public TargetDTO(Landmark lm) {
            this.name = lm.getName();
            this.riddle = lm.getRiddle();
            this.latitude = lm.getLatitude();
            this.longitude = lm.getLongitude();
        }
    
        public String getName() { return name; }
        public String getRiddle() { return riddle; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}

