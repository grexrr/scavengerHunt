package com.scavengerhunt.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.LandmarkRepository;
import com.scavengerhunt.repository.UserRepository;

@Service
public class EloUtils {
    private UserRepository userRepo;
    private LandmarkRepository landmarkRepo;

    @Autowired
    public EloUtils(UserRepository userRepo, LandmarkRepository landmarkRepo) {
        this.userRepo = userRepo;
        this.landmarkRepo = landmarkRepo;
    }

    public void updateRating(String userId, String landmarkId, long secondsUsed, boolean correct) {
        User user = userRepo.findById(userId).orElseThrow();
        Landmark landmark = landmarkRepo.findById(landmarkId).orElseThrow();

        // else
    }
}
