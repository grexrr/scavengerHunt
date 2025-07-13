package com.scavengerhunt.utils;

import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;

public class GeoUtils {
    
    private static final double EARTH_RADIUS = 6371000; // meters

    /**
     * Calculate distance in meters between two lat/lng coordinates using Haversine formula.
     */
    public static double distanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static Landmark detectedLandmark(List<String> candidatesId, Player player, GameDataRepository gameDataRepo){
        Polygon playerCone = player.getPlayerCone();
        double playerLat = player.getLatitude();
        double playerLng = player.getLongitude();
        double playerAngle = player.getAngle();

        System.out.println("[GeoUtils] Detecting landmarks from " + candidatesId.size() + " candidates");
        System.out.println("[GeoUtils] Player position: " + playerLat + ", " + playerLng + " @ " + playerAngle);
        System.out.println("[GeoUtils] Player cone span: " + player.getSpanDeg() + "°, radius: " + player.getRadiusMeters() + "m");
        System.out.println("[GeoUtils] Player cone polygon: " + playerCone.getNumPoints() + " points");
        System.out.println("[GeoUtils] Player cone bounds: " + playerCone.getEnvelopeInternal());

        Landmark selectedLandmark = null;
        double minAngleDiff = Double.MAX_VALUE;

        for (String lmid : candidatesId) {
            Landmark lm = gameDataRepo.findLandmarkById(lmid);
            if (lm == null) {
                System.out.println("[GeoUtils] Landmark not found for ID: " + lmid);
                continue;
            }
            
            System.out.println("[GeoUtils] Checking landmark: " + lm.getName() + " (ID: " + lmid + ")");
            System.out.println("[GeoUtils] Landmark geometry: " + (lm.getGeometry() != null ? "exists" : "null"));
            
            if (lm.getGeometry() == null) {
                System.out.println("[GeoUtils] Skipping landmark with null geometry");
                continue;
            }
            
            try {
                Polygon lmPolygon = convertToJtsPolygon(lm.getGeometry());
                System.out.println("[GeoUtils] Converted polygon for " + lm.getName() + ": " + lmPolygon.getNumPoints() + " points");
                System.out.println("[GeoUtils] Polygon bounds: " + lmPolygon.getEnvelopeInternal());
                
                // Calculate distance to landmark
                double targetLat = lm.getCentroid().get("latitude");
                double targetLng = lm.getCentroid().get("longitude");
                double distance = distanceInMeters(playerLat, playerLng, targetLat, targetLng);
                System.out.println("[GeoUtils] Distance to " + lm.getName() + ": " + distance + "m");
                
                if (lmPolygon.intersects(playerCone)){
                    System.out.println("[GeoUtils] Landmark intersects with player cone");
                    double angleToTarget = calculateTargetAngle(playerLat, playerLng, targetLat, targetLng);
                    double angleDiff = minimalAngleDiff(playerAngle, angleToTarget);

                    System.out.println("[GeoUtils] Angle to target: " + angleToTarget + ", angle diff: " + angleDiff);

                    if (angleDiff < minAngleDiff){
                        minAngleDiff = angleDiff;
                        selectedLandmark = lm;
                        System.out.println("[GeoUtils] New best landmark: " + lm.getName() + " with angle diff: " + angleDiff);
                    }   
                } else {
                    System.out.println("[GeoUtils] Landmark does not intersect with player cone (distance: " + distance + "m)");
                    // Add angle calculation for debugging
                    double angleToTarget = calculateTargetAngle(playerLat, playerLng, targetLat, targetLng);
                    double angleDiff = minimalAngleDiff(playerAngle, angleToTarget);
                    System.out.println("[GeoUtils] Angle to " + lm.getName() + ": " + angleToTarget + ", angle diff: " + angleDiff + ", player angle: " + playerAngle);
                }
            } catch (Exception e) {
                System.out.println("[GeoUtils] Error processing landmark " + lm.getName() + ": " + e.getMessage());
            }
        }
        
        System.out.println("[GeoUtils] Final selected landmark: " + (selectedLandmark != null ? selectedLandmark.getName() : "null"));
        return selectedLandmark;
    }

    /** 
     * Helper Functions
     */

    public static Polygon convertToJtsPolygon(GeoJsonPolygon geo) {
        // GeoJsonPolygon coordination：first element is outer-parameter（shell），then inner（holes）
        var shell = geo.getCoordinates().get(0); // GeoJsonLineString
        Coordinate[] coords = shell.getCoordinates().stream()
            .map(p -> new Coordinate(p.getX(), p.getY())) // GeoJSON: lng, lat
            .toArray(Coordinate[]::new);
        // ensure closed polygon
        if (!coords[0].equals2D(coords[coords.length - 1])) {
            coords = Arrays.copyOf(coords, coords.length + 1);
            coords[coords.length - 1] = coords[0];
        }
        return new GeometryFactory().createPolygon(coords);
    }


    private static double calculateTargetAngle(double lat1, double lng1, double lat2, double lng2){
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        double angle = Math.toDegrees(Math.atan2(dLng, dLat));
        return angle < 0 ? angle + 360 : angle;
    }

    private static double minimalAngleDiff(double angle1, double angle2) {
        double diff = Math.abs(angle1 - angle2);
        return diff > 180 ? 360 - diff : diff;
    }
}
