package com.scavengerhunt.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GeoUtilsTest {

    @Test
    void distanceInMeters_UCC_to_CorkCity() {
        double lat1 = 51.8985, lon1 = -8.4756; // Cork city
        double lat2 = 51.8936, lon2 = -8.4920; // UCC

        double distance = GeoUtils.distanceInMeters(lat1, lon1, lat2, lon2);

        assertTrue(distance > 1000 && distance < 2000, "Distance should be ~1.3km, was: " + distance);
    }

    @Test
    void distanceInMeters_samePoint() {
        double lat1 = 51.8985, lon1 = -8.4756; // Cork city
        double lat2 = 51.8985, lon2 = -8.4756; // UCC

        double distance = GeoUtils.distanceInMeters(lat1, lon1, lat2, lon2);

        assertEquals(0, distance, 0.001, "Same point should be distance 0");;
    }

    @Test
    void distanceInMeters_northSouth_positiveDistance() {
        double lat1 = 51.8930, lon = -8.4920;
        double lat2 = 51.8940;

        double distance = GeoUtils.distanceInMeters(lat1, lon, lat2, lon);
        assertTrue(distance > 0, "Moving north should be positive distance");
        assertTrue(distance < 200, "~0.001 degree latitude is ~111m, should be < 200m");
    }

    @Test
    void distanceInMeters_eastWest_positiveDistance() {
        double lat = 51.8930;
        double lon1 = -8.4920, lon2 = -8.4900;

        double distance = GeoUtils.distanceInMeters(lat, lon1, lat, lon2);
        assertTrue(distance > 0, "Moving east should be positive distance");
        assertTrue(distance < 200, "~0.002 degree longitude at this latitude is ~140m");
    }

    @Test
    void distanceInMeters_isSymmetric() {
        double d1 = GeoUtils.distanceInMeters(51.8930, -8.4920, 51.8940, -8.4900);
        double d2 = GeoUtils.distanceInMeters(51.8940, -8.4900, 51.8930, -8.4920);
        assertEquals(d1, d2, 0.001, "Distance A→B should equal B→A");
    }
}
