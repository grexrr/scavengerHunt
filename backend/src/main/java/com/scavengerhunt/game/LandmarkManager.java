package com.scavengerhunt.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;

/**
 * Manages all landmarks used during the game round.
 * - Load landmarks from data source
 * - Maintain solved/unsolved status
 * - Provide helpers to select next suitable target
 */
public class LandmarkManager {

    private final GameDataRepository gameDataRepo;
    private final LandmarkProcessorClient landmarkAgentClient;

    private String currentCity;

    private List<Landmark> allRoundLandmarks; // within radius
    private List<String> allLocalLandmarkIds; // all landmark-Ids of current city

    // public LandmarkManager() {
    //     this.allRoundLandmarks = new ArrayList<>();
    // }

    public LandmarkManager(GameDataRepository gameDataRepo, LandmarkProcessorClient landmarkAgentClient, String city) {
        this.allRoundLandmarks = new ArrayList<>();
        this.gameDataRepo = gameDataRepo;
        this.landmarkAgentClient = landmarkAgentClient;
        this.currentCity = city;
        this.allLocalLandmarkIds = this.gameDataRepo.loadLandmarkIdByCity(city); // mvp style, expandable
    }

    public void getRoundLandmarksIdWithinRadius(double lat, double lng, double radiusMeters) {
        GeoJsonPoint point = new GeoJsonPoint(lng, lat);
        Distance radius = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);
        List<Landmark> nearBy = gameDataRepo.findByLocationNear(point, radius);
        this.landmarkAgentClient.ensureLandmarkMeta(nearBy);
        this.allRoundLandmarks = nearBy;
    }

    /**
     * Getter & Setter
     */
    public void setCurrentCity(String city) {
        this.currentCity = city;
        this.allLocalLandmarkIds = this.gameDataRepo.loadLandmarkIdByCity(city);
    }

    public String getCurrentCity() { return this.currentCity; }

    public List<Landmark> getAllRouLandmark() { return this.allRoundLandmarks; }

    public List<String> getAllLocalLandmarkIds() { return this.allLocalLandmarkIds; }
}
