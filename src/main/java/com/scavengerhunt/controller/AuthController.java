package com.scavengerhunt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.UserRepository;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody IdentityRequest request){
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("Username already exists.");
        }
        User newUser = new User(request.getUsername(), request.getPassword());
        userRepo.save(newUser);
        return ResponseEntity.ok("[Backend] Register success!");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody IdentityRequest request) {
        return userRepo.findByUsername(request.getUsername())
        .filter(user -> user.getPassword().equals(request.getPassword()))
        .map(user -> ResponseEntity.ok(user.getPlayerId()))
        .orElse(ResponseEntity.status(401).body("[Backend] Invalid username or password."));
    }


    @PostMapping("/logout")
    public String logout() {
        return "Logout";
    }
    
    //DTO

    public static class IdentityRequest {
        private String username;
        private String password;
        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
