package com.scavengerhunt.utils;

import org.springframework.stereotype.Service;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.LandmarkRepository;
import com.scavengerhunt.repository.UserRepository;

@Service
public class EloCalculator {

    private User user;
    private UserRepository userRepo;
    private LandmarkRepository landmarkRepo;

    public EloCalculator(String userId, UserRepository userRepo, LandmarkRepository landmarkRepo) {
        this.userRepo = userRepo;
        this.landmarkRepo = landmarkRepo;
        this.user = this.userRepo.findByUserId(userId).orElseThrow();
    }

    public void updateRating(String landmarkId, long secondsUsed, boolean correct) {
        
        Landmark landmark = landmarkRepo.findById(landmarkId).orElseThrow();

        // else
    }
}
