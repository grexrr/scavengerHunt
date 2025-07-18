package com.scavengerhunt.utils;

import java.time.LocalDateTime;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.GameDataRepository;

public class EloCalculator {

    private User user;
    private GameDataRepository gameDataRepo;

    private int maxRiddleDurationMinutes = 30;
    
    public EloCalculator(String userId, GameDataRepository gameDataRepo, int maxRiddleDurationMinutes) {
        this.gameDataRepo = gameDataRepo;
        this.user = this.gameDataRepo.getUserById(userId);
        this.maxRiddleDurationMinutes = maxRiddleDurationMinutes;
    }

    public void updateRating(boolean isCorrect){
        if(isCorrect){
            System.out.println("[ELO] Correct! Update Rating");
        } else {
            System.out.println("[ELO] Wrong! Update Rating");
        }
    }

    public void updateRating(String landmarkId, long riddleSeconds, boolean isCorrect) {
        // Reset user to get latest data from database
        this.user = this.gameDataRepo.getUserById(this.user.getUserId());
        
        Landmark landmark = gameDataRepo.findLandmarkById(landmarkId);
        
        double[] dynamicK = dynamicK(this.user, landmark);
        double userK = dynamicK[0];
        double landmarkK = dynamicK[1];

        double[] hshsResult = hshsExpectation(riddleSeconds, user.getRating(), landmark.getRating(), isCorrect);
        double hshs = hshsResult[0];
        double expectation = hshsResult[1];

        double delta = (hshs - expectation);

        double userNewRating = this.user.getRating() + userK * delta;
        double landmarkNewRating = landmark.getRating() - landmarkK * delta;

        gameDataRepo.updateUserRating(this.user.getUserId(), userNewRating);
        gameDataRepo.updateLandmarkRating(landmark.getId(), landmarkNewRating);
    }

    private double[] dynamicK(User user, Landmark landmark){
        double K = 0.0075;
        double Kmax = 4.0;
        double Kmin = 0.5;

        double userNewUncertainty = updateUncertainty(user, "default");
        double landmarkNewUncertainty = updateUncertainty(landmark, "default");

        double userK = K * (1 + Kmax * userNewUncertainty - Kmin * landmarkNewUncertainty);
        double landmarkK = K * (1 + Kmax * landmarkNewUncertainty - Kmin * userNewUncertainty);

        return new double[]{userK, landmarkK};
    }

    private <T> double updateUncertainty(T entity, String mode){
        if (mode == "default"){
            if (entity instanceof User){
                //1. Acquire User.lastGameAt
                User user = (User) entity;
                LocalDateTime lastGameAt = user.getLastGameAt();
                
                //2. if User.lastGameAt is null, day diff = 30
                int dayDiff = 30;
                if (lastGameAt == null) {
                    // update User.lastGameAt
                    gameDataRepo.updateUserLastGameAt(user.getUserId(), LocalDateTime.now());
                } else {
                    // else calculate dayDiff
                    dayDiff = (int) java.time.Duration.between(lastGameAt, LocalDateTime.now()).toDays();
                }

                // 3. Calculate U
                double uncertainty = user.getUncertainty() - (1 / 40.0) + (1 / 30.0) * dayDiff;
                // Clamp uncertainty to [0, 1] range
                uncertainty = Math.max(0, Math.min(1, uncertainty));
                // update newU;
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
                // Clamp uncertainty to [0, 1] range
                uncertainty = Math.max(0, Math.min(1, uncertainty));
                
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
        
        double timeLimitSeconds = this.maxRiddleDurationMinutes * 60; //convert to seconds for calculation

        //for now use default mode with 1/10 discrimination until further testing
        double discrimination = discrimination(timeLimitSeconds, "default");

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

    private double discrimination(double timeLimitSeconds, String mode){
        if (mode == "default"){
            return 1.0 / 10;
        } else {
            return 1.0 / timeLimitSeconds;
        }
    }

}
