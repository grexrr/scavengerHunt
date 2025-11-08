package com.scavengerhunt.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.scavengerhunt.game.GameSession;

@Service
public class GameSessionService {
    
    private Map<String, GameSession> sessionMap = new HashMap<>();
    
    public GameSession getSession(String userId) {
        return sessionMap.get(userId);
    }
    
    public void putSession(String userId, GameSession session) {
        sessionMap.put(userId, session);
    }
    
    public void removeSession(String userId) {
        sessionMap.remove(userId);
        System.out.println("[GameSessionService] Session cleared for user: " + userId);
    }
    
    public boolean hasSession(String userId) {
        return sessionMap.containsKey(userId);
    }
}