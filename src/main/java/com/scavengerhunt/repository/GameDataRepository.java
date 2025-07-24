package com.scavengerhunt.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;

@Repository
public class GameDataRepository {
    /**
     * Centralized repository for game data operations.
     * Manages all data access for PlayerStateManager and LandmarkManager.
     */

    private final LandmarkRepository landmarkRepo;
    private final UserRepository userRepo;

    public GameDataRepository(LandmarkRepository landmarkRepo, UserRepository userRepo) {
        this.landmarkRepo = landmarkRepo;
        this.userRepo = userRepo;
    }

    // ==================== Landmark Operations ====================

    public List<Landmark> loadLandmarks() {
        return landmarkRepo.findAll();
    }

    public List<String> getAllLandmarkIds() {
        return landmarkRepo.findAllId();
    }

    public Landmark findLandmarkById(String id) {
        return landmarkRepo.findById(id).orElse(null);
    }

    public List<String> loadLandmarkIdByCity(String city) {
        System.out.println("[GameDataRepository] Querying for city: '" + city + "'");
        return landmarkRepo.findByCity(city).stream()
            .map(Landmark::getId)
            .toList();
    }

    public double getLandmarkRatingById(String landmarkId){
        Landmark landmark = findLandmarkById(landmarkId);
        Double rating = (landmark != null) ? landmark.getRating() : null;
        return (rating != null) ? rating : 0.5;
    }
   
    public void updateLandmarkRating(String landmarkId, Double rating) {
        Landmark landmark = findLandmarkById(landmarkId);
        if (landmark != null) {
            if (landmark.getRating() == null) {
                landmark.setRating(0.5); // !!!! init default as 0.5 
            }
            landmark.setRating(rating);
            landmarkRepo.save(landmark);
            System.out.println("[✓] Landmark rating updated: " + rating);
        }
    }

    // public void updateLandmarkUncertainty(String landmarkId, Double uncertainty) {
    //     Landmark landmark = findLandmarkById(landmarkId);
    //     if (landmark != null) {
    //         if (landmark.getUncertainty() == null) {
    //             landmark.setUncertainty(0.5);
    //         }
    //         landmark.setUncertainty(uncertainty);
    //         landmarkRepo.save(landmark);
    //     }
    // }

    public void updateLandmarkLastAnswered(String landmarkId, LocalDateTime time) {
        Landmark landmark = findLandmarkById(landmarkId);
        if (landmark != null) {
            landmark.setLastAnswered(time);
            landmarkRepo.save(landmark);
        }
    }

    // ==================== User Operations ====================

    public User getUserById(String userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public void updateUserRating(String userId, Double rating) {
        User user = getUserById(userId);
        if (user != null) {
            if (user.getRating() == null) {
                user.setRating(0.5);
            }
            user.setRating(rating);
            userRepo.save(user);
            System.out.println("[✓] User rating updated: " + rating);
        }
    }

    // public void updateUserUncertainty(String userId, Double uncertainty) {
    //     User user = getUserById(userId);
    //     if (user != null) {
    //         if (user.getUncertainty() == null) {
    //             user.setUncertainty(0.5);
    //         }
    //         user.setUncertainty(uncertainty);
    //         userRepo.save(user);
    //     }
    // }

    public void updateUserLastGameAt(String userId, LocalDateTime time) {
        User user = getUserById(userId);
        if (user != null) {
            user.setLastGameAt(time);
            userRepo.save(user);
        }
    }

    // ==================== Repositories ====================

    public UserRepository getUserRepo() {
        return this.userRepo;
    }

    public LandmarkRepository getLandmarkRepo() {
        return this.landmarkRepo;
    }
}
