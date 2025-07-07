package com.scavengerhunt.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a Landmark object.
 */

@Document(collection = "landmarks")
public class Landmark {
    
    @Id
    private String id; 
    
    private String name;
    private String city;

    private Map<String, Double> centroid = new HashMap<>();  // keys: latitude, longitude
    private GeoJsonPolygon geometry;

    private double rating = 0.0; //range += 3 guarenteed by algorithm
    private double uncertainty = 0.5; // Glicko / CAP style init
    private LocalDateTime lastAnswered; //day

    private String riddle;

    /**
     * Constructs a new Landmark object.
     *
     * @param id The unique identifier of the landmark, cannot be null
     * @param name The name of the landmark, cannot be null or an empty string
     * @param riddle The riddle of the landmark, can be null
     * @param latitude The latitude of the landmark, range is -90 to 90
     * @param longitude The longitude of the landmark, range is -180 to 180
     * @param isSolved Whether the landmark has been solved
     * @throws IllegalArgumentException If id or name is null or an empty string
     */

    public Landmark(){}

    public Landmark(String name, String city, double latitude, double longitude) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("id and name cannot be null or empty");
        }
        this.name = name;
        this.city = city;
        this.centroid.put("latitude", latitude);
        this.centroid.put("longitude", longitude);
    }

    /**
     * Getters & Setters
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Map<String, Double> getCentroid() {
        return centroid;
    }

    public void setCentroid(Map<String, Double> centroid) {
        this.centroid = centroid;
    }

    public void setCentroid(double latitude, double longitude) {
        this.centroid.put("latitude", latitude);
        this.centroid.put("longitude", longitude);
    }

    public double getLatitude() {
        return centroid.get("latitude");
    }

    public double getLongitude() {
        return centroid.get("longitude");
    }

    public GeoJsonPolygon getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJsonPolygon geometry) {
        this.geometry = geometry;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
    }

    public LocalDateTime getLastAnswered() {
        return lastAnswered;
    }

    public void getLastAnswered(LocalDateTime lastAnswered) {
        this.lastAnswered = lastAnswered;
    }

    public String getRiddle() {
        return riddle;
    }

    public void setRiddle(String riddle) {
        this.riddle = riddle;
    }
}
