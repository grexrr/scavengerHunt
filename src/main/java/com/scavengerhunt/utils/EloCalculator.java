package com.scavengerhunt.utils;

import org.springframework.stereotype.Service;

import com.scavengerhunt.repository.GameDataRepository;

@Service
public class EloCalculator {

    private String userRepo;
    private GameDataRepository gameDataRepo;

    public EloCalculator(String userId, GameDataRepository gameDataRepo) {
        this.userRepo = userId;
        this.gameDataRepo = gameDataRepo;
       
    }

    public void updateRating(String landmarkId, long secondsUsed, boolean correct) {
        
        // else
    }
}
