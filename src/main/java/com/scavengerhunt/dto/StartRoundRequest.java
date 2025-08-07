package com.scavengerhunt.dto;

public class StartRoundRequest {
    private String userId;
    private double latitude;
    private double longitude;
    private double angle;
    private double radiusMeters;
    private String language;
    private String style;

    
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

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
