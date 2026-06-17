package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;

public class LandmarkManagerTest {

    @Mock private GameDataRepository mockGameDataRepo;
    @Mock private LandmarkProcessorClient mockLandmarkProcessorClient;

    private LandmarkManager testLandmarkManager;

    private Landmark glucksman = new Landmark(
        "686fe2fd5513908b37be306d",
        "Glucksman Gallery",
        "Cork",
        51.894741757894735,
        -8.490317963157894);

    private Landmark quad = new Landmark(
        "686fe2fd5513908b37be3071",
        "The Quad",
        "Cork",
        51.89372202222222,
        -8.492224097916667);

    private Landmark boole = new Landmark(
        "6895327b04e4917e0d875789",
        "Boole Library",
        "Cork",
        51.89285984,
        -8.491245088);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockGameDataRepo.loadLandmarkIdByCity("Cork")).thenReturn(
            Arrays.asList(
                glucksman.getId(),
                quad.getId(),
                boole.getId()
            )
        );

        when(mockGameDataRepo.findLandmarkById(glucksman.getId())).thenReturn(glucksman);
        when(mockGameDataRepo.findLandmarkById(quad.getId())).thenReturn(quad);
        when(mockGameDataRepo.findLandmarkById(boole.getId())).thenReturn(boole);

        testLandmarkManager = new LandmarkManager(mockGameDataRepo, mockLandmarkProcessorClient, "Cork");
    }

    @Test
    void getRoundLandmarksIdWithinRadius_50m_returnOne() {
        // at glucksman's front
        testLandmarkManager.getRoundLandmarksIdWithinRadius(51.894442, -8.4902510, 50);

        List<Landmark> res = testLandmarkManager.getAllRouLandmark();
        assertEquals(1, res.size());
        assertEquals("Glucksman Gallery", res.get(0).getName());
    }

    @Test
    void getRoundLandmarksIdWithinRadius_500m_returnAll() {
        // at glucksman's front
        testLandmarkManager.getRoundLandmarksIdWithinRadius(51.894442, -8.4902510, 500);

        List<Landmark> res = testLandmarkManager.getAllRouLandmark();
        assertEquals(3, res.size());
    }

    @Test
    void getRoundLandmarksIdWithinRadius_10m_returnEmpty() {
        // ucc gate
        testLandmarkManager.getRoundLandmarksIdWithinRadius(51.894985, -8.489127500, 10);
        List<Landmark> res = testLandmarkManager.getAllRouLandmark();

        assertTrue(res.isEmpty(), "No landmarks within 10m of this position");
    }

    @Test
    void getAllLandmarkIds_returnsAll() {
        List<String> res = testLandmarkManager.getAllLocalLandmarkIds();
        assertEquals(res.size(), 3);
        assertTrue(res.contains(glucksman.getId()));
        assertTrue(res.contains(quad.getId()));
        assertTrue(res.contains(boole.getId()));
    }
}
