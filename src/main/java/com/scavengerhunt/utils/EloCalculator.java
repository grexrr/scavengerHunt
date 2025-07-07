package com.scavengerhunt.utils;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.GameDataRepository;

@Service
public class EloCalculator {

    private User user;
    private GameDataRepository gameDataRepo;

    private double timeLimitMinutes = 30;
    
    public EloCalculator(String userId, GameDataRepository gameDataRepo) {
        this.gameDataRepo = gameDataRepo;
        this.user = this.gameDataRepo.getUserById(userId);
    }

    public void updateRating(boolean isCorrect){
        if(isCorrect){
            System.out.println("[ELO] Correct! Update Rating");
        } else {
            System.out.println("[ELO] Wrong! Update Rating");
        }
        
    }

    public void updateRating(String landmarkId, long riddleMinutes, boolean correct) {
        
        // else
    }

    private double[] dynamicK(User user, Landmark landmark){
        return new double[]{3.0, 4.0};
    }

    private <T> double updateUncertainty(T entity, String mode){
        if (mode == "default"){
            if (entity instanceof User){
                //1. Acquire User.lastGameAt
                User user = (User) entity;
                LocalDateTime lastGameAt = user.getLastGameAt();
                int dayDiff = 30;

                //2. if User.lastGameAt is null, day diff = 30
                //   else calculate day diff
                if (lastGameAt == null) {
                    // lastGameAt = LocalDateTime.now().minusDays(dayDiff);
                    gameDataRepo.updateUserLastGameAt(user.getUserId(), LocalDateTime.now());
                } else {
                    dayDiff = (int) java.time.Duration.between(lastGameAt, LocalDateTime.now()).toDays();
                }
                // 3. Calculate U
                double uncertainty = user.getUncertainty() - (1 / 40.0) + (1 / 30.0) * dayDiff;
                gameDataRepo.updateUserUncertainty(user.getUserId(), uncertainty);
                return uncertainty;

            } else if (entity instanceof Landmark){
                Landmark landmark = (Landmark) entity;
                LocalDateTime lastAnswered = landmark.getLastAnswered();
                int dayDiff = 30;

                if (lastAnswered == null) {
                    gameDataRepo.updateLandmarkLastAnswered(landmark.getId(), LocalDateTime.now());
                } else {
                    dayDiff = (int) java.time.Duration.between(lastAnswered, LocalDateTime.now()).toDays();
                }
                
                double uncertainty = landmark.getUncertainty() - (1 / 40.0) + (1 / 30.0) * dayDiff;
                
                gameDataRepo.updateLandmarkUncertainty(landmark.getId(), uncertainty);
                return uncertainty;
            } else {
                return 0.5;
            }
        } else if (mode == "test"){
            if (entity instanceof User){
                User user = (User) entity;
                return user.getUncertainty();
            } else if (entity instanceof Landmark) {
                Landmark landmark = (Landmark) entity;
                return landmark.getUncertainty();
            } else {
                return 0.5;
            }
        } else {
            return 0.5; // default return value
        }
    }

    private double[] hshsExpectation(double timeUsedSeconds, double userRating, double landmarkRating, boolean isCorrect){
        
        double timeLimitSeconds = this.timeLimitMinutes * 60; //convert to seconds for calculation

        double discrimination = discrimination(timeUsedSeconds, "default");
        double timeComponent = discrimination * (timeLimitSeconds - timeUsedSeconds);

        double hshs = isCorrect ? timeComponent : -timeComponent; 
        
        double delta = userRating - landmarkRating;
        if (Math.abs(delta) < 1e-6){
            delta = 1e-6;
        }

        double weightAbilityDiff = 2 * discrimination * timeLimitSeconds * delta;
        double expTerm = Math.exp(weightAbilityDiff);
        double expectation = discrimination * timeLimitSeconds *  ((expTerm + 1) / (expTerm - 1)) - (1 / delta);

        return new double[]{hshs, expectation};
    }

    private double discrimination(double timeUsedSeconds, String mode){
        if (mode != "default"){
            return 1.0 / timeUsedSeconds;
        } else {
            return 1.0 / 10;
        }
    }

}
