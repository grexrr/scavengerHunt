package com.scavengerhunt.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.scavengerhunt.model.Landmark;

@SpringBootTest
public class LandmarkRepositoryIntegrationTest {

    @Autowired private LandmarkRepository landmarkRepo;

    private Landmark glucksman;
    private Landmark quad;
    private Landmark boole;

    @BeforeEach
    void setup() {
        landmarkRepo.deleteAll();

        glucksman = new Landmark(
            "Glucksman Gallery", "Cork", 51.894741757894735, -8.490317963157894);
        quad = new Landmark(
            "The Quad", "Cork", 51.89372202222222, -8.492224097916667);
        boole = new Landmark(
            "Boole Library", "Cork", 51.89285984, -8.491245088);

        landmarkRepo.save(glucksman);
        landmarkRepo.save(quad);
        landmarkRepo.save(boole);
    }

    private List<String> namesWithinRadius(double lat, double lng, double radiusMeters) {
        GeoJsonPoint point = new GeoJsonPoint(lng, lat);
        Distance radius = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);
        GeoResults<Landmark> results = landmarkRepo.findByLocationNear(point, radius);
        return results.getContent().stream()
                .map(GeoResult::getContent) // GeoResult<Landmark>
                .map(Landmark::getName)
                .collect(Collectors.toList());
    }

    @Test
    void findByLocationNear_50m_returnsOnlyGlucksman() {
        List<String> names = namesWithinRadius(51.894442, -8.4902510, 50);

        assertEquals(1, names.size());
        assertTrue(names.contains("Glucksman Gallery"));
    }

    @Test
    void findByLocationNear_500m_returnsAllThree() {
        List<String> names = namesWithinRadius(51.894442, -8.4902510, 500);

        assertEquals(3, names.size());
        assertTrue(names.contains("Glucksman Gallery"));
        assertTrue(names.contains("The Quad"));
        assertTrue(names.contains("Boole Library"));
    }

    @Test
    void findByLocationNear_10m_returnsEmpty() {
        List<String> names = namesWithinRadius(51.894985, -8.489127500, 10);

        assertTrue("No landmarks within 10m of this position", names.isEmpty());
    }
}
