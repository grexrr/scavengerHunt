package com.scavengerhunt.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
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
    private GeoJsonPolygon geometry;

    private Double rating;
    // private Double uncertainty;
    private LocalDateTime lastAnswered;

    private String riddle;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    public Landmark() {
    }

    public Landmark(String name, String city, Double lat, Double lng) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("id and name cannot be null or empty");
        }
        this.name = name;
        this.city = city;
        this.location = new GeoJsonPoint(lng, lat);
    }

    // for testing
    public Landmark(String id, String name, String city, Double lat, Double lng) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("id and name cannot be null or empty");
        }
        this.id = id;
        this.name = name;
        this.city = city;
        this.location = new GeoJsonPoint(lng, lat);
    }

    // Getters and Setters

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

    public Double getLatitude() {
        return this.location != null ? location.getY() : null;
    }

    public Double getLongitude() {
        return this.location != null ? location.getX() : null;
    }

    public void setLocation(double lat, double lng) {
        this.location = new GeoJsonPoint(lng, lat);
    }

    public GeoJsonPolygon getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJsonPolygon geometry) {
        this.geometry = geometry;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    // public Double getUncertainty() { return uncertainty; }
    // public void setUncertainty(Double uncertainty) { this.uncertainty =
    // uncertainty; }

    public LocalDateTime getLastAnswered() {
        return lastAnswered;
    }

    public void setLastAnswered(LocalDateTime lastAnswered) {
        this.lastAnswered = lastAnswered;
    }

    public String getRiddle() {
        return riddle;
    }

    public void setRiddle(String riddle) {
        this.riddle = riddle;
    }
}
