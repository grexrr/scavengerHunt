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

    private List<Landmark> allLocalLandmarks; 
    private GameDataRepository gameDataRepo;

    public LandmarkManager() {
        this.allLocalLandmarks = new ArrayList<>();
    }

    public LandmarkManager(GameDataRepository dataRepo) {
        this.allLocalLandmarks = new ArrayList<>();
        this.gameDataRepo = dataRepo;
        this.allLocalLandmarks = this.gameDataRepo.loadLandmarks(); 
    }

    //TODO: Feed-in allLocalLandmarks within Radius

    public List<Landmark> getLocalLandmarksWithinRadius(double lat, double lng, double radiusMeters) {
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

    public List<Landmark> getAllLandmarks() {
        return this.allLocalLandmarks;
    }    
}
