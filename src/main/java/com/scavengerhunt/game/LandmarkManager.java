package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scavengerhunt.utils.GeoUtils;

/**
 * Manages the status of landmarks.
 * Provides the following functions:
 *     Load/register landmarks (hardcoded in MVP phase)
 *     Filter suitable targets based on "player-defined search range"
 *     Ensure no already solved landmarks are selected (query from PlayerStateManager)
 */


 public class LandmarkManager {

    private List<Landmark> localLandmarks = new ArrayList<>();        //candidate landmarks of current round
    private List<Landmark> unsolvedLandmarks = new ArrayList<>();

    public LandmarkManager(List<Landmark> loadeLandmarks){
        this.localLandmarks = loadeLandmarks;
    }

    /**
     * Core Functions
     */

    // Locate next riddle during the game 
    // This function will be used by PuzzleManager to select and control the next landmark
    public Landmark selectNearestToPlayer(PlayerStateManager playerState) {
        double lat = playerState.getPlayer().getLatitude();
        double lng = playerState.getPlayer().getLongitude();
        return selectNearestTo(lat, lng);
    }
    
    public Landmark selectNextLandmark(Landmark lastSolvedLandmark) {
        return selectNearestTo(lastSolvedLandmark.getLatitude(), lastSolvedLandmark.getLongitude());
    }
    

    // Update unsolved landmarks according to player status
    public void updateUnsolved (PlayerStateManager playerState){
        this.unsolvedLandmarks = localLandmarks.stream()
            .filter(lm -> !playerState.isLandmarkSolved(lm))
            .collect(Collectors.toList());
    }
    
    // Riddle pooling: Used when initialize a new round
    // Future modification necessary
    public List<Landmark> filterLocalInRadius(PlayerStateManager playerState, double radiusMeters) {
        double playerLat = playerState.getPlayer().getLatitude();
        double playerLng = playerState.getPlayer().getLongitude();
        return localLandmarks.stream()
            .filter(lm -> isWithinRadius(lm, playerLat, playerLng, radiusMeters))
            .collect(Collectors.toList());
    }

    /**
     * Helper Functions
     */
    private boolean isWithinRadius(Landmark landmark, double centerLat, double centerLng, double radiusMeters) {
        double dist = GeoUtils.distanceInMeters(centerLat, centerLng, landmark.getLatitude(), landmark.getLongitude());
        return dist <= radiusMeters;
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
    
}

