package com.scavengerhunt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class StartRoundRequest {

    @Schema(
        description = "Unique user identifier",
        example = "uuid-string",
        required = true
    )
    private String userId;

    @Schema(
        description = "Latitude in decimal degrees", 
        example = "40.7128",
        required = true
    )
    private double latitude;

    @Schema(
        description = "Longitude in decimal degrees", 
        example = "-74.0060",
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
        description = "Search Radius of the Game Round", 
        example = "500 (unit: meters)",
        required = true
    )
    private double radiusMeters;

    @Schema(
        description = "Player riddle language preference", 
        example = "500 (unit: meters)",
        defaultValue = "English",
        required = true
    )
    private String language = "English";

    @Schema(
        description = "Player riddle style preference", 
        example = "medieval",
        defaultValue = "medieval",
        required = true
    )
    private String style = "medieval";

    
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
