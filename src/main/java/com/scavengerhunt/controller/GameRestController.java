package com.scavengerhunt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.game.Landmark;
import com.scavengerhunt.ui.UIController;


@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private final UIController uiController;

    public GameRestController(UIController uiController) {
        this.uiController = uiController;
    }

    @PostMapping("/init")
    public ResponseEntity<String> initGame(@RequestBody PlayerInitRequest request) {
        System.out.println("[DEBUG] initGame() called");
        
        System.out.println("[DEBUG] Coordination Received: " + request.getLatitude() + ", " + request.getLongitude());

        uiController.initGame(
            request.getLatitude(), 
            request.getLongitude(), 
            request.getAngle()
            );
        return ResponseEntity.ok("Game Initialized");
    }

    @PostMapping("/start-round")
    public ResponseEntity<String> startRound(@RequestBody StartRoundRequest request) {
        System.out.println("[DEBUG] startRound called: " + request.getLatitude() + ", radius: " + request.getRadius());

        // update player coord
        uiController.getSession().getPlayerState().updatePlayerPosition(
            request.getLatitude(), request.getLongitude(), request.getAngle()
        );

        // init riddle
        uiController.getSession().applySearchArea(request.getRadius());
        return ResponseEntity.ok("Round started.");
    }
    
    @GetMapping("/target")
    public ResponseEntity<?> getCurrentTarget() {
        Landmark target = uiController.getSession().getCurrentTarget();
        if (target == null) {
            return ResponseEntity.status(404).body("No target selected.");
        }
    
        return ResponseEntity.ok(new TargetDTO(target));
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

