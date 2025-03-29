package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;

import com.scavengerhunt.data.GameDataRepo;
import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages all landmarks used during the game round.
 * - Load landmarks from data source
 * - Maintain solved/unsolved status
 * - Provide helpers to select next suitable target
 */
public class LandmarkRepo {

    // All landmarks loaded for the current round
    private List<Landmark> localLandmarks = new ArrayList<>();

    // Filtered views
    private List<Landmark> unsolvedLandmarks;
    private List<Landmark> solvedLandmarks = new ArrayList<>();

    public void loadLandmarks(GameDataRepo repo) {
        this.localLandmarks = repo.loadLandmarks();
        this.unsolvedLandmarks = new ArrayList<>(localLandmarks);
        this.solvedLandmarks.clear();
    }

    public void markSolved(Landmark landmark) {
        if (unsolvedLandmarks.contains(landmark)) {
            unsolvedLandmarks.remove(landmark);
            solvedLandmarks.add(landmark);
        }
    }

    public Landmark selectNearestToPlayer(PlayerStateManager playerState) {
        double lat = playerState.getPlayer().getLatitude();
        double lng = playerState.getPlayer().getLongitude();
        return selectNearestTo(lat, lng);
    }
    
    
    private Landmark selectNearestTo(double refLat, double refLng) {
        return unsolvedLandmarks.stream()
            .min((l1, l2) -> {
                double d1 = GeoUtils.distanceInMeters(refLat, refLng, l1.getLatitude(), l1.getLongitude());
                double d2 = GeoUtils.distanceInMeters(refLat, refLng, l2.getLatitude(), l2.getLongitude());
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    public List<Landmark> getUnsolvedLandmarks() {
        return this.unsolvedLandmarks;
    }

    public Landmark getLastSolved() {
        if (solvedLandmarks.isEmpty()) return null;
        return solvedLandmarks.get(solvedLandmarks.size() - 1);
    }

    public Landmark selectNextLandmark(Landmark lastSolvedLandmark) {
        return selectNearestTo(lastSolvedLandmark.getLatitude(), lastSolvedLandmark.getLongitude());
    }
    
    public List<Landmark> getSolvedLandmarks(){
        return this.solvedLandmarks;
    }
}
