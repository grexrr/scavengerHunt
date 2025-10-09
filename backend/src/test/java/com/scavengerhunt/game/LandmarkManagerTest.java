package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
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
import com.scavengerhunt.utils.GeoUtils;

public class LandmarkManagerTest {
        @Mock
        private GameDataRepository mockGameDataRepo;

        @Mock
        private LandmarkProcessorClient mockLandmarkProcessorClient;

        private LandmarkManager landmarkManager;

        private Landmark glucksman;
        private Landmark quad;
        private Landmark boole;

        @BeforeEach
        public void setup() {
                MockitoAnnotations.openMocks(this);

                glucksman = new Landmark(
                                "686fe2fd5513908b37be306d",
                                "Glucksman Gallery",
                                "Cork",
                                51.894741757894735,
                                -8.490317963157894);

                quad = new Landmark(
                                "686fe2fd5513908b37be3071",
                                "The Quad",
                                "Cork",
                                51.89372202222222,
                                -8.492224097916667);

                boole = new Landmark(
                                "6895327b04e4917e0d875789",
                                "Boole Library",
                                "Cork",
                                51.89285984,
                                -8.491245088);

                // 🔧 在创建 LandmarkManager 之前设置 Mock
                when(mockGameDataRepo.loadLandmarkIdByCity("Cork"))
                                .thenReturn(Arrays.asList(glucksman.getId(), quad.getId(), boole.getId()));

                when(mockGameDataRepo.findLandmarkById(glucksman.getId()))
                                .thenReturn(glucksman);

                when(mockGameDataRepo.findLandmarkById(quad.getId()))
                                .thenReturn(quad);

                when(mockGameDataRepo.findLandmarkById(boole.getId()))
                                .thenReturn(boole);

                doNothing().when(mockLandmarkProcessorClient).ensureLandmarkMeta(anyList());

                this.landmarkManager = new LandmarkManager(
                                mockGameDataRepo,
                                mockLandmarkProcessorClient,
                                "Cork");
        }

        @Test
        public void testGetRoundLandmarksIdWithinRadius() {
                // Mock 设置已移到 @BeforeEach 中
                
                double[] testCoord = {51.894, -8.490};
                
                System.out.println("Distance to Glucksman Gallery: " +
                                GeoUtils.distanceInMeters(
                                testCoord[0],
                                testCoord[1],
                                glucksman.getLatitude(),
                                glucksman.getLongitude()));

                System.out.println("Distance to The Quad: " +
                                GeoUtils.distanceInMeters(
                                testCoord[0],
                                testCoord[1],
                                quad.getLatitude(),
                                quad.getLongitude()));
                System.out.println("Distance to Boole Library: " +
                                GeoUtils.distanceInMeters(
                                testCoord[0],
                                testCoord[1],
                                boole.getLatitude(),
                                boole.getLongitude()));
                
                // Distance to Glucksman Gallery: 85.31678631920943
                // Distance to The Quad: 155.7176055758049
                // Distance to Boole Library: 152.882590974321

                // 调用方法
                landmarkManager.getRoundLandmarksIdWithinRadius(testCoord[0], testCoord[1], 100);

                // 验证结果（通过公共方法）
                List<Landmark> roundLandmarks = landmarkManager.getAllRouLandmark();
                assertEquals(1, roundLandmarks.size());
                assertEquals("Glucksman Gallery", roundLandmarks.get(0).getName());
        }
}
