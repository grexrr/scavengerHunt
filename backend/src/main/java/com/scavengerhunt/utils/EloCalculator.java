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


    public void updateRating(String landmarkId, long riddleSeconds, boolean isCorrect) {
        // Refresh user and landmark from DB
        this.user = this.gameDataRepo.getUserById(this.user.getUserId());
        Landmark landmark = gameDataRepo.findLandmarkById(landmarkId);
    
        // ======= Fallback rating defaults =======
        double userRating = (user.getRating() != null && !Double.isNaN(user.getRating())) ? user.getRating() : 0.5;
        double landmarkRating = (landmark.getRating() != null && !Double.isNaN(landmark.getRating())) ? landmark.getRating() : 0.5;
    
        // ======= Uncertainty-aware learning rates =======
        double[] dynamicK = dynamicK(this.user, landmark);
        double userK = dynamicK[0];
        double landmarkK = dynamicK[1];
    
        // ======= HSHS =======
        double[] hshsResult = hshsExpectation(riddleSeconds, userRating, landmarkRating, isCorrect);
        double hshs = hshsResult[0];
        double expectation = hshsResult[1];
        double delta = hshs - expectation;
    
     
        double userNewRating = userRating + userK * delta;
        double landmarkNewRating = landmarkRating - landmarkK * delta;
    
   
        if (Double.isNaN(userNewRating)) userNewRating = 0.5;
        if (Double.isNaN(landmarkNewRating)) landmarkNewRating = 0.5;
    
        // // ======= Logs =======
        // System.out.println("====== [EloCalc] Rating Update ======");
        // System.out.println("[EloCalc] Target Landmark: " + landmark.getName() + " (ID: " + landmark.getId() + ")");
        // System.out.println("[EloCalc] User ID: " + user.getUserId());
        // System.out.println("[EloCalc] Time used: " + riddleSeconds + " seconds");
        // System.out.println("[EloCalc] Answer correct? " + isCorrect);
        // System.out.printf("[EloCalc] Current User Rating: %.4f → %.4f (K=%.5f)\n", userRating, userNewRating, userK);
        // System.out.printf("[EloCalc] Current Landmark Rating: %.4f → %.4f (K=%.5f)\n", landmarkRating, landmarkNewRating, landmarkK);
        // System.out.printf("[EloCalc] Delta: %.4f, hshs: %.4f, expected: %.4f\n", delta, hshs, expectation);
        // System.out.println("==================================");
    

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
        if (mode.equals("default")) {
            int dayDiff = 30;
    
            if (entity instanceof User) {
                User user = (User) entity;
                LocalDateTime last = user.getLastGameAt();
                if (last != null) {
                    dayDiff = (int) java.time.Duration.between(last, LocalDateTime.now()).toDays();
                }
            } 
            else if (entity instanceof Landmark) {
                Landmark landmark = (Landmark) entity;
                LocalDateTime last = landmark.getLastAnswered();
                if (last != null) {
                    dayDiff = (int) java.time.Duration.between(last, LocalDateTime.now()).toDays();
                }
            } 
            else {
                return 0.5;
            }
    
            // calculate uncertainty purely from time
            double uncertainty = 0.5 - (1.0 / 40.0) + (1.0 / 30.0) * dayDiff;
            return Math.max(0, Math.min(1, uncertainty));
        }
    
        // test mode: always return 0.5 for simplicity
        if (mode.equals("test")) {
            return 0.5;
        }
    
        return 0.5;
    }
    

    private double[] hshsExpectation(double timeUsedSeconds, double userRating, double landmarkRating, boolean isCorrect){
        double timeLimitSeconds = this.maxRiddleDurationMinutes * 60;
        double discrimination = discrimination(timeLimitSeconds, "default");
    
        double timeComponent = discrimination * (timeLimitSeconds - timeUsedSeconds);
        double hshs = isCorrect ? timeComponent : -timeComponent; 
    
        double delta = userRating - landmarkRating;
        if (Math.abs(delta) < 1e-6){
            delta = 1e-6;
        }
    
        // overflow proof
        double weightAbilityDiff = 2 * discrimination * timeLimitSeconds * delta;
        weightAbilityDiff = Math.max(-700, Math.min(700, weightAbilityDiff));  
    
        double expTerm = Math.exp(weightAbilityDiff);
        double expectation = discrimination * timeLimitSeconds * ((expTerm + 1) / (expTerm - 1)) - (1 / delta);
    
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
