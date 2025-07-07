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

    private double rating = 0.0; //range += 3 guarenteed by algorithm
    private double uncertainty = 0.5; // Glicko / CAP style init
    private double displayRating; //updatable by simple sigmoid

    // timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastGameAt; 

    public User(){}
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now(); // 设置创建时间 (Set creation time)
        setUserId();
    }

    public User(String username, String password, Boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now(); // 设置创建时间 (Set creation time)
        setAdmin(isAdmin);
        setUserId();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId() {
        this.userId = UUID.randomUUID().toString();
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Integer> getSolvedLandmarkIds() {
        return solvedLandmarkIds;
    }

    public void setSolvedLandmarkIds(List<Integer> solvedLandmarkIds) {
        this.solvedLandmarkIds = solvedLandmarkIds;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
    }

    public double getDisplayRating() {
        return displayRating;
    }

    public void setDisplayRating(double displayRating) {
        this.displayRating = displayRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getLastGameAt() {
        return lastGameAt;
    }

    public void setLastGameAt(LocalDateTime lastGameAt) {
        this.lastGameAt = lastGameAt;
    }
    
}
