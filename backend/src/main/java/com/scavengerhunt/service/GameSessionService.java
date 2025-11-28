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
        GameSession session = sessionMap.get(userId);
        if (session != null) {
            session.getPlayerState().setGameFinished();
            System.out.println("[GameSessionService] Player " + session.getUserId() + " session finished and cleared.");
        } else {
            System.out.println("[GameSessionService] Session not found for user: " + userId);
        }
        sessionMap.remove(userId);
    }
    
    public boolean hasSession(String userId) {
        return sessionMap.containsKey(userId);
    }
}