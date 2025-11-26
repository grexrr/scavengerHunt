package com.scavengerhunt.dto;

public class PlayerPositionRequest {
    private String userId;
    private double latitude;
    private double longitude;
    private double angle;

    private double spanDeg = 30;
    private double coneRadiusMeters = 50;
    // private String city = "Cork";

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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getSpanDeg() {
        return spanDeg;
    }

    public void setSpanDeg(double spanDeg) {
        this.spanDeg = spanDeg;
    }

    public double getConeRadiusMeters() {
        return coneRadiusMeters;
    }

    public void setConeRadiusMeters(double coneRadiusMeters) {
        this.coneRadiusMeters = coneRadiusMeters;
    }

    // public String getCity() {
    //     return city;
    // }
    // public void setCity(String city) {
    //     this.city = city;
    // }
}
