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

    private List<Landmark> allRoundLandmarks; //within radius
    private List<String> allLocalLandmarkIds;   //all landmarks of current city
    private GameDataRepository gameDataRepo;

    public LandmarkManager() {
        this.allRoundLandmarks = new ArrayList<>();
    }

    public LandmarkManager(GameDataRepository dataRepo) {
        this.allRoundLandmarks = new ArrayList<>();
        this.gameDataRepo = dataRepo;
        this.allRoundLandmarks = this.gameDataRepo.loadLandmarks(); 
    }

    public List<String> getRoundLandmarksIdWithinRadius(double lat, double lng, double radiusMeters) {
        System.out.println("[LandmarkManager] Checking radius: " + radiusMeters + "m around (" + lat + ", " + lng + ")");
        for (Landmark lm : allRoundLandmarks) {
            double dist = GeoUtils.distanceInMeters(lat, lng, lm.getLatitude(), lm.getLongitude());
            System.out.println("[LandmarkManager] " + lm.getName() + " distance = " + dist + "m");
        }
    
        List<String> filtered = this.allRoundLandmarks.stream()
            .filter(lm -> GeoUtils.distanceInMeters(lat, lng, lm.getLatitude(), lm.getLongitude()) <= radiusMeters)
            .map(lm -> lm.getId())
            .collect(Collectors.toList());

        System.out.println("[LandmarkManager] Selected landmarks in range:");
        for (String lmid : filtered) {
            // search lm name in db
            String landmarkName = gameDataRepo.findLandmarkNameById(lmid);
            System.out.println("  - " + landmarkName);
        }

        return filtered;
    }

    public List<Landmark> getAllLandmarks() {
        return this.allRoundLandmarks;
    }    

    public void setAllLocalLandmarkIds(String city){
        this.allLocalLandmarkIds = gameDataRepo.loadLandmarkIdByCity(city);
    }

    public List<String> getAllLocalLandmarkIds() {
        return this.allLocalLandmarkIds;
    }
}
