package com.scavengerhunt.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.dto.UserIdentityRequest;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.UserRepository;
import com.scavengerhunt.security.JwtTokenProvider;
import com.scavengerhunt.service.GameSessionService;

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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private GameSessionService gameSessionService;


    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Operation(
        summary = "Register new user",
        description = "Creates a new player account with username and password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserIdentityRequest request){
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {

            log.debug("Username {} already exists!", request.getUsername());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username already exists.");
            return ResponseEntity.status(409).body(errorResponse);
        }

        String userEmail = request.getEmail();
        if (userEmail != null && userRepo.findByEmail(userEmail).isPresent()) {

            log.debug("Email {} already exists!", request.getEmail());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email already exists.");
            return ResponseEntity.status(409).body(errorResponse);
        }


        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(request.getUsername(), encodedPassword);
        if (userEmail != null) {
            newUser.setEmail(userEmail);
        }

        String language = request.getPreferredLanguage();
        if (language != null && !language.trim().isEmpty()) {
            newUser.setPreferredLanguage(language.toLowerCase());
        } else {
            newUser.setPreferredLanguage("english");
        }

        String style = request.getPreferredStyle();
        if (style != null && !style.trim().isEmpty()) {
            newUser.setPreferredStyle(style.toLowerCase());
        } else {
            newUser.setPreferredStyle("medieval");
        }

        if (request.getCreatedAt() != null && !request.getCreatedAt().trim().isEmpty()) {
            try {
                LocalDateTime createdAt = LocalDateTime.parse(request.getCreatedAt());
                newUser.setCreatedAt(createdAt);
            } catch (Exception e) {

                log.debug("LocalDateTime parse failed for user: {}", request.getUsername());

                newUser.setCreatedAt(LocalDateTime.now());
            }
        } else {
            newUser.setCreatedAt(LocalDateTime.now());
        }

        userRepo.save(newUser);

        log.info("Registered new user {}", newUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        return ResponseEntity.ok(response);
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
        @ApiResponse(responseCode = "400", description = "Username or email is required."),
        @ApiResponse(responseCode = "404", description = "Invalid username/email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserIdentityRequest request) {

        String username = request.getUsername();
        String email = request.getEmail();

        Optional<User> user;

        if (username != null && !username.trim().isEmpty()) {

            log.info("{} log in with username", username);

            user = userRepo.findByUsername(username);
        } else if (email != null && !email.trim().isEmpty()) {

            log.info("{} log in with email", email);

            user = userRepo.findByEmail(email);
        } else {

            log.debug("Username or email not provided");

            return ResponseEntity.status(400).body(
                Map.of("error", "[Backend] Username or email is required.")
            );
        }

        return user
            .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
            .map(u -> {
                String role = u.getAdmin() ? "ADMIN" : "PLAYER";
                String token = tokenProvider.generateToken(u.getUserId(), role);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("token", token);
                userInfo.put("userId", u.getUserId());
                userInfo.put("username", u.getUsername());
                userInfo.put("role", role);
                return ResponseEntity.ok(userInfo);
            })
            .orElseGet(
                () -> {
                    log.info("Failed login attempt for {}", username != null ? username : email);
                    return ResponseEntity.status(404).body(Map.of("error", "[Backend] Invalid username or password."));
                }
            );
    }

    @Operation(
    summary = "User logout",
    description = "Logs out the current user and clears game session"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "UserId is required")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        String userId = currentUserId();

        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.status(400).body(
                Map.of("error", "UserId is required.")
            );
        }

        if (gameSessionService.hasSession(userId)) {

            log.debug("{} logout, session cleared", userId);

            gameSessionService.removeSession(userId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logout successful. Game session cleared.");

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get user profile",
        description = "Returns complete user profile information and preferences."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        String userId = currentUserId();
        return userRepo.findByUserId(userId)
            .map(user -> {
                Map<String, Object> profile = new HashMap<>();
                profile.put("userId", userId);
                profile.put("username", user.getUsername());
                profile.put("email", user.getEmail() != null ? user.getEmail() : "");
                profile.put("role", user.getAdmin() ? "ADMIN": "PLAYER");
                profile.put("preferredLanguage", user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "english");
                profile.put("preferredStyle", user.getPreferredStyle() != null ? user.getPreferredStyle() : "medieval");
                return ResponseEntity.ok(profile);
            }).orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "User not found")));
    }

    @Operation(
    summary = "Update user profile",
    description = "Updates user profile information including username, email, and preferences"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "409", description = "Already exists")
    })
    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UserIdentityRequest request) {
        String userId = currentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "UserId is required"));
        }

        return userRepo.findByUserId(userId)
            .map(user -> {
                if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                    String newUsername = request.getUsername();
                    Optional<User> existingUser = userRepo.findByUsername(newUsername);

                    if(existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("error", "Username already exists.");
                        return ResponseEntity.status(409).body(errorResponse);
                    } else {
                        user.setUsername(newUsername);
                    }
                }

                if (request.getEmail() != null) {
                    String newEmail = request.getEmail().trim();
                    if (newEmail.isEmpty()) {
                        user.setEmail(null);
                    } else {
                        Optional<User> existingEmailUser = userRepo.findByEmail(newEmail);
                        if (existingEmailUser.isPresent() && !existingEmailUser.get().getUserId().equals(userId)) {
                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("error", "Email already exists.");
                            return ResponseEntity.status(409).body(errorResponse);
                        }
                        user.setEmail(newEmail);
                    }
                }

                if (request.getPreferredLanguage() != null) {
                    user.setPreferredLanguage(request.getPreferredLanguage().toLowerCase());
                } else {
                    user.setPreferredLanguage("english");
                }

                if (request.getPreferredStyle() != null) {
                    user.setPreferredStyle(request.getPreferredStyle().toLowerCase());
                } else {
                    user.setPreferredStyle("medieval");
                }

                userRepo.save(user);

                log.debug("Profile updated for {}", userId);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Profile updated successfully");
                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "User not found")));
    }

    private String currentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

