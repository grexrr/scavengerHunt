package com.scavengerhunt.model;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * Represents a player entity in the game.
 * Contains basic information including position and orientation.
 * Logic and progression are handled by PlayerStateManager.
 */
public class Player {

    private double latitude;
    private double longitude;
    private double angle;
    private String city;
    
    //playerCone
    private Polygon playerCone;
    private double spanDeg = 30;
    private double radiusMeters = 50;
    private int resolution = 50;

    private String playerId;
    private String nickname;

    /**
     * Creates a new Player at the specified position and orientation
     * @param latitude The initial latitude position
     * @param longitude The initial longitude position  
     * @param angle The initial facing angle in degrees
     */
    
    public Player(double latitude, double longitude, double angle) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.angle = angle;
        this.playerCone = setPlayerViewCone(latitude, longitude, angle, this.spanDeg, this.radiusMeters, this.resolution);
        acquireCity(); // MVP as "Cork"
    }

    /**
     * Core
     */

     public Polygon setPlayerViewCone(double latitude, double longitude, double angle, double spanDeg, double radiusMeters, int resolution){
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(longitude, latitude)); // lng first, starting with player coord

        double step = spanDeg / resolution;
        double startAngle = angle - spanDeg / 2;

        for (int i = 0; i <= resolution; i++) {
            double theta = Math.toRadians(startAngle + i * step);
            double dLat = (radiusMeters * Math.cos(theta)) / 111320.0;
            double dLng = (radiusMeters * Math.sin(theta)) / (111320.0 * Math.cos(Math.toRadians(latitude)));
            coords.add(new Coordinate(longitude + dLng, latitude + dLat));
        }
    
        coords.add(coords.get(0)); // close
        this.playerCone = new GeometryFactory().createPolygon(coords.toArray(new Coordinate[0]));
        return this.playerCone;
    }

    /**
     * Update player cone based on current position and angle
     */
    public void updatePlayerCone() {
        this.playerCone = setPlayerViewCone(this.latitude, this.longitude, this.angle, this.spanDeg, this.radiusMeters, this.resolution);
    }

    /**
     * Getter & Setter
     */

    public String getCity() {
        return this.city;
    }

    public void acquireCity(){
        this.city = "Cork";
    }

    public void setCity(String city){
        this.city = city;
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

    public Polygon getPlayerCone(){
        return this.playerCone;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerNickname() {
        return nickname;
    }

    public void setPlayerNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public double getSpanDeg() {
        return spanDeg;
    }
    
    public double getRadiusMeters() {
        return radiusMeters;
    }
    
    public int getResolution() {
        return resolution;
    }
}
