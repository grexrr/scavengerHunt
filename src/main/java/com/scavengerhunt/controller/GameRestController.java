package com.scavengerhunt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.ui.UIController;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private final UIController uiController = new UIController();

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
}

