package com.scavengerhunt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static Map<String, User> userStore = new HashMap<>();

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        if (userStore.containsKey(request.getUsername())) {
            return ResponseEntity.status(409).body("Username already exists.");
        }
        String newPlayerID = UUID.randomUUID().toString();
        User newUser = new User(request.getUsername(), request.getPassword(), newPlayerID);
        userStore.put(request.getUsername(), newUser);
        return ResponseEntity.ok("Register success!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        User user = userStore.get(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password.");
        }

        return ResponseEntity.ok(user.getPlayerId()); 
    }

    //DTO

    public static class RegisterRequest {
        private String username;
        private String password;
        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class User{
        private String username;
        private String password;
        private String playerId;
        
        public User(String username, String password, String playerID){
            this.setUsername(username);
            this.setPassword(password);
            this.setPlayerId(playerID);
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        private String playerId;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
    }
}
