package com.scavengerhunt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PlayerPositionRequest {

    @Schema(
        description = "Unique user identifier",
        example = "408808b8-777c-469a-867d-dd5e7d5e38e2",
        required = true
    )
    private String userId;

    @Schema(
        description = "Latitude in decimal degrees", 
        example = "51.894964",
        required = true
    )
    private double latitude;

    @Schema(
        description = "Longitude in decimal degrees", 
        example = "-8.489178",
        required = true
    )
    private double longitude;

    @Schema(
        description = "Facing angle in degrees (0 = north)", 
        example = "135",
        required = true
    )
    private double angle;

    @Schema(
        description = "Field-of-view span in degrees", 
        defaultValue = "30"
    )
    private double spanDeg = 30;

    @Schema(
        description = "Cone radius in meters", 
        defaultValue = "50"
    )
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

