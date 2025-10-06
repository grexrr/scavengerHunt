package com.scavengerhunt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.dto.UserIdentityRequest;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    
    @Operation(
        summary = "Register new user",
        description = "Creates a new player account with username and password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserIdentityRequest request){
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("Username already exists.");
        }
        User newUser = new User(request.getUsername(), request.getPassword());
        newUser.setCreatedAt(null);
        userRepo.save(newUser);
        return ResponseEntity.ok("[Backend] Register success!");
    }

    @Operation(
        summary = "User login",
        description = "Authenticates user and returns user info."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(responseCode = "404", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserIdentityRequest request) {
        return userRepo.findByUsername(request.getUsername())
        .filter(user -> user.getPassword().equals(request.getPassword()))
        .map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());  // Changed from userName to username
            userInfo.put("role", user.getAdmin() ? "ADMIN": "PLAYER");
            return ResponseEntity.ok(userInfo);
        })
        .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "[Backend] Invalid username or password.")));
    }

    @Operation(
        summary = "User logout",
        description = "Logs out the current user"
    )
    @PostMapping("/logout")
    public String logout() {
        return "Logout";
    }
}

