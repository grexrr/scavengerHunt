package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages all landmarks used during the game round.
 * - Load landmarks from data source
 * - Maintain solved/unsolved status
 * - Provide helpers to select next suitable target
 */
public class LandmarkManager {

    private GameDataRepository gameDataRepo;
    private String currentCity;

    private List<Landmark> allRoundLandmarks; //within radius
    private List<String> allLocalLandmarkIds; //all landmark-Ids of current city
    

    public LandmarkManager() {
        this.allRoundLandmarks = new ArrayList<>();
    }

    public LandmarkManager(GameDataRepository gameDataRepo, String city) {
        this.allRoundLandmarks = new ArrayList<>();
        this.gameDataRepo = gameDataRepo;
        this.currentCity = city;
        this.allLocalLandmarkIds = this.gameDataRepo.loadLandmarkIdByCity(city); // mvp style, expandable
        System.out.println("[LandmarkManager] Initialized for city: " + city + " with " + this.allLocalLandmarkIds.size() + " local landmarks");
    }

    public void getRoundLandmarksIdWithinRadius(double lat, double lng, double radiusMeters) {
        System.out.println("[LandmarkManager] Checking radius: " + radiusMeters + "m around (" + lat + ", " + lng + ")");
        
        List<Landmark> filtered = allLocalLandmarkIds.stream()
            .map(id -> gameDataRepo.findLandmarkById(id)) 
            .filter(lm -> lm != null)
            .filter(lm -> {
                double dist = GeoUtils.distanceInMeters(lat, lng, lm.getLatitude(), lm.getLongitude());
                System.out.println("[LandmarkManager] " + lm.getName() + " distance = " + dist + "m");
                return dist <= radiusMeters;
            })
            .collect(Collectors.toList());
        
        System.out.println("[LandmarkManager] Selected landmarks in range:");
        for (Landmark landmark : filtered) {
            System.out.println("  - " + landmark.getName());
        }

        this.allRoundLandmarks = filtered;
    }

    /**
     * Update the current city and reload local landmark IDs
     */
    public void setCurrentCity(String city) {
        this.currentCity = city;
        this.allLocalLandmarkIds = this.gameDataRepo.loadLandmarkIdByCity(city);
    }

    /**
     * Get the current city
     */
    public String getCurrentCity() {
        return this.currentCity;
    }

    /** 
     * Getter & Setter
     */

    public List<Landmark> getAllRouLandmark(){
        return this.allRoundLandmarks;
    }

    public List<String> getAllLocalLandmarkIds(){
        return this.allLocalLandmarkIds;
    }
}
