package com.scavengerhunt.dto;

public class StartRoundRequest {
    private String userId;
    private double latitude;
    private double longitude;
    private double radiusMeters;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getRadiusMeters() {
        return radiusMeters;
    }
    public void setRadiusMeters(double radiusMeters) {
        this.radiusMeters = radiusMeters;
    }
}
