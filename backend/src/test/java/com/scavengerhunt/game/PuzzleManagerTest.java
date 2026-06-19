package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.client.dto.GenerateRiddleRequest;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;

public class PuzzleManagerTest {

    @Mock private GameDataRepository mockGameDataRepo;
    @Mock private PuzzleAgentClient mockPuzzleAgentClient;

    private PuzzleManager testPuzzleManager;

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

        testPuzzleManager = new PuzzleManager(mockGameDataRepo, mockPuzzleAgentClient);
    }

    @Test
    void testFullInit(){
        String mockSessionId = "session-123";
        List<Landmark> targetPool = Arrays.asList(
            new Landmark(
                "lm-001",
                "Cork",
                50.00,
                50.00
            ),
            new Landmark(
                "lm-002",
                "Cork",
                50.00,
                50.00
            ),
            new Landmark(
                "lm-003",
                "Cork",
                50.00,
                50.00
            )
        );
        String language = "English";
        String style = "Medieval";

        testPuzzleManager.initialize(mockSessionId, targetPool, language, style);

        assertEquals(testPuzzleManager.getTargetPool().size(), 3);
        assertEquals(testPuzzleManager.getStyle(), "Medieval");
        assertEquals(testPuzzleManager.getLanguage(), "English");
    }

    @Test
    void testInit_noLanguageStyle(){
        String mockSessionId = "session-123";
        List<Landmark> targetPool = Arrays.asList(
            new Landmark(
                "lm-001",
                "Cork",
                50.00,
                50.00
            ),
            new Landmark(
                "lm-002",
                "Cork",
                50.00,
                50.00
            ),
            new Landmark(
                "lm-003",
                "Cork",
                50.00,
                50.00
            )
        );

        testPuzzleManager.initialize(mockSessionId, targetPool, null, null);

        assertEquals(testPuzzleManager.getStyle(), "Medieval");
        assertEquals(testPuzzleManager.getLanguage(), "English");
    }

    @Test
    void getRiddleForLandmark_apiSuccess_returnRiddle(){
        String mockLandmarkId = "lm-123";
        String mockRiddle = "I have spires but no church. What am I?";

        when(mockGameDataRepo.getLandmarkRatingById(mockLandmarkId)).thenReturn(0.5);
        when(mockPuzzleAgentClient.generateRiddle(any(GenerateRiddleRequest.class))).thenReturn(mockRiddle);

        assertEquals(testPuzzleManager.getRiddleForLandmark(mockLandmarkId), mockRiddle);
        verify(mockPuzzleAgentClient, times(1)).generateRiddle(any(GenerateRiddleRequest.class));
    }

    @Test
    void getRiddleForLandmark_apiThrows_returnsDefaultRiddle(){
        String mockLandmarkId = "lm-123";

        when(mockGameDataRepo.getLandmarkRatingById(mockLandmarkId)).thenReturn(0.5);
        when(mockPuzzleAgentClient.generateRiddle(any(GenerateRiddleRequest.class))).thenThrow(new RuntimeException("Mock python server down"));

        String res = testPuzzleManager.getRiddleForLandmark(mockLandmarkId);

        assertEquals("Default Riddle", res, "Should fall back to default riddle on API failure");
    }

    @Test
    void getRiddleForLandmark_calledTwice(){
        String landmarkId = "lm-001";
        when(mockGameDataRepo.getLandmarkRatingById(landmarkId)).thenReturn(0.5);
        when(mockPuzzleAgentClient.generateRiddle(any(GenerateRiddleRequest.class))).thenReturn("Some riddle");

        testPuzzleManager.getRiddleForLandmark(landmarkId);
        testPuzzleManager.getRiddleForLandmark(landmarkId);

        verify(mockPuzzleAgentClient, times(2)).generateRiddle(any(GenerateRiddleRequest.class));
    }
}
