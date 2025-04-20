package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages all landmarks used during the game round.
 * - Load landmarks from data source
 * - Maintain solved/unsolved status
 * - Provide helpers to select next suitable target
 */
public class LandmarkRepo {

    private List<Landmark> allLocalLandmarks = new ArrayList<>(); 
    private GameDataRepo gameDataRepo;

    public LandmarkRepo() {
        this.allLocalLandmarks = new ArrayList<>();
    }

    public LandmarkRepo(GameDataRepo dataRepo) {
        this.allLocalLandmarks = new ArrayList<>();
        this.gameDataRepo = dataRepo;
        this.loadLandmarks();;
    }
    
    public void loadLandmarks() {
        this.allLocalLandmarks = this.gameDataRepo.loadLandmarks(); 
    }
   
    public List<Landmark> getAllLandmarks() {
        return this.allLocalLandmarks;
    }

    public List<Landmark> getAllLandmarksWithinRadius(double lat, double lng, double radiusMeters) {
        System.out.println("[DEBUG] Checking radius: " + radiusMeters + "m around (" + lat + ", " + lng + ")");
        for (Landmark lm : allLocalLandmarks) {
            double dist = GeoUtils.distanceInMeters(lat, lng, lm.getLatitude(), lm.getLongitude());
            System.out.println("[DEBUG] " + lm.getName() + " distance = " + dist + "m");
        }
    
        List<Landmark> filtered = this.allLocalLandmarks.stream()
            .filter(lm -> GeoUtils.distanceInMeters(lat, lng, lm.getLatitude(), lm.getLongitude()) <= radiusMeters)
            .collect(Collectors.toList());

        System.out.println("[DEBUG] Selected landmarks in range:");
        for (Landmark lm : filtered) {
            System.out.println("  - " + lm.getName());
        }

        return filtered;
    }
    
}
