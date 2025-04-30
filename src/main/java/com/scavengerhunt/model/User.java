package com.scavengerhunt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String playerId;

    private String username;
    private String password;
    private List<Integer> solvedLandmarkIds = new ArrayList<>();

    public User(){}
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        setPlayerId();
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
    public String getPlayerId() {
        return playerId;
    }
    public void setPlayerId() {
        this.playerId = UUID.randomUUID().toString();
    }

    public List<Integer> getSolvedLandmarkIds() {
        return solvedLandmarkIds;
    }

    public void setSolvedLandmarkIds(List<Integer> solvedLandmarkIds) {
        this.solvedLandmarkIds = solvedLandmarkIds;
    }
}
