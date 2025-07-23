package com.scavengerhunt.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String userId;

    private String username;
    private String password;
    private List<Integer> solvedLandmarkIds = new ArrayList<>();

    private Boolean admin = false;

    private Double rating;
    // private Double uncertainty;
    private Double displayRating;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastGameAt;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.userId = UUID.randomUUID().toString();
    }

    public User(String username, String password, Boolean isAdmin) {
        this(username, password);
        this.admin = isAdmin;
    }

    // Getters and Setters

    public String getUserId() { return userId; }
    public void setUserId(String id) { this.userId = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Integer> getSolvedLandmarkIds() { return solvedLandmarkIds; }
    public void setSolvedLandmarkIds(List<Integer> ids) { this.solvedLandmarkIds = ids; }

    public Boolean getAdmin() { return admin; }
    public void setAdmin(Boolean admin) { this.admin = admin; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    // public Double getUncertainty() { return uncertainty; }
    // public void setUncertainty(Double uncertainty) { this.uncertainty = uncertainty; }

    public Double getDisplayRating() { return displayRating; }
    public void setDisplayRating(Double displayRating) { this.displayRating = displayRating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getLastGameAt() { return lastGameAt; }
    public void setLastGameAt(LocalDateTime lastGameAt) { this.lastGameAt = lastGameAt; }
}

