package com.scavengerhunt.utils;

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
}
