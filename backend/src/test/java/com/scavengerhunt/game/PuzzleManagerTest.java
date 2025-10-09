package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.repository.GameDataRepository;

public class PuzzleManagerTest {
    @Mock
    private GameDataRepository mockGameDataRepo;

    @Mock
    private PuzzleAgentClient mockPuzzleAgentClient;

    private PuzzleManager puzzleManager;  
    
    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        puzzleManager = new PuzzleManager(mockGameDataRepo, mockPuzzleAgentClient);
    }

    @Test
    public void testGetRiddleForLandmark_success(){
        String landmarkId = "686fe2fd5513908b37be3070";
        String expectedRiddle = "Riddle for Bool Library...";
        
        // Simulate Repository returning a rating
        when(mockGameDataRepo.getLandmarkRatingById(landmarkId))
            .thenReturn(0.5);
        
        // Simulate API returning a result (anyMap() = accepts any payload)
        when(mockPuzzleAgentClient.generateRiddle(anyMap()))
            .thenReturn(expectedRiddle);
        
        // Act: Call the method under test
        String result = puzzleManager.getRiddleForLandmark(landmarkId);
        
        // Assert: Verify the result
        assertEquals(expectedRiddle, result, "Riddle matches...");
        
        // Verify if the method was called
        verify(mockPuzzleAgentClient, times(1)).generateRiddle(anyMap());
    }

    @Test
    public void testGetRiddleForLandmark_APIFails() {
        
        String landmarkId = "landmark456";
        
        when(mockGameDataRepo.getLandmarkRatingById(landmarkId))
            .thenReturn(0.7);
        
        // Simulate API exception
        when(mockPuzzleAgentClient.generateRiddle(anyMap()))
            .thenThrow(new RuntimeException("API is down"));
        
        // Act
        String result = puzzleManager.getRiddleForLandmark(landmarkId);
        
        // Assert: Should return default riddle
        assertEquals("Default Riddle", result);
    }
}
