package com.scavengerhunt.utils;

import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.LandmarkRepository;

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

    public static Landmark detectedLandmark(List<String> candidatesId, Player player, LandmarkRepository landmarkRepo){
        Polygon playerCone = player.getPlayerCone();
        double playerLat = player.getLatitude();
        double playerLng = player.getLongitude();
        double playerAngle = player.getAngle();

        Landmark selectedLandmark = null;
        double minAngleDiff = Double.MAX_VALUE;

        for (String lmid : candidatesId) {
            Landmark lm = landmarkRepo.findById(lmid).orElse(null); // needs front end to response to this!
            if (lm == null) continue;
            
            Polygon lmPolygon = convertToJtsPolygon(lm.getGeometry()); // convert from MongoPolygon to JTS
            
            if (lmPolygon.intersects(playerCone)){
                double targetLat = lm.getCentroid().get("latitude");
                double targetLng = lm.getCentroid().get("longitude");

                double angleToTarget = calculateTargetAngle(playerLat, playerLng, targetLat, targetLng);
                double angleDiff = minimalAngleDiff(playerAngle, angleToTarget);

                if (angleDiff < minAngleDiff){
                    minAngleDiff = angleDiff;
                    selectedLandmark = lm;
                }   
            }
        }
        return selectedLandmark;
    }

    /** 
     * Helper Functions
     */

    private static Polygon convertToJtsPolygon(GeoJsonPolygon geo) {
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
