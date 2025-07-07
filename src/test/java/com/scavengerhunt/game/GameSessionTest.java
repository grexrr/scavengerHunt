package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.EloCalculator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GameSessionTest {

    @Mock
    private GameDataRepository gameDataRepository;
    
    @Mock
    private PlayerStateManager playerState;
    
    @Mock
    private LandmarkManager landmarkManager;
    
    @Mock
    private PuzzleManager puzzleManager;
    
    @Mock
    private EloCalculator eloManager;

    private GameSession gameSession;
    private Player mockPlayer;
    private Landmark landmark1, landmark2, landmark3;

    @BeforeEach
    void setUp() {
        // Create test landmark data
        landmark1 = new Landmark("Glucksman Gallery", "Cork", 51.8947417, -8.4903179);
        landmark1.setId("landmark1");
        
        landmark2 = new Landmark("Honan Chapel", "Cork", 51.8935347, -8.4895357);
        landmark2.setId("landmark2");
        
        landmark3 = new Landmark("Boole Library", "Cork", 51.8927959, -8.4914071);
        landmark3.setId("landmark3");

        // Create mock player
        mockPlayer = new Player(51.8940, -8.4902, 90.0);
        mockPlayer.setPlayerId("test-user");

        // Set up basic Mock behavior
        when(playerState.getPlayer()).thenReturn(mockPlayer);
        when(playerState.isGameFinished()).thenReturn(false);
        when(puzzleManager.getRiddleForLandmark(anyString())).thenReturn("Test riddle");
        
        // Set up setGameFinished behavior to change the return value of isGameFinished
        doAnswer(invocation -> {
            when(playerState.isGameFinished()).thenReturn(true);
            return null;
        }).when(playerState).setGameFinished();

        // Create GameSession instance
        gameSession = new GameSession("test-user", gameDataRepository, playerState, landmarkManager, puzzleManager);
    }

    /**
     * Test Scenario 1: Can complete a full game successfully
     * Verify that the game can correctly complete all landmarks
     */
    @Test
    void scenario1() {
        // Prepare test data - 2 landmarks
        List<Landmark> candidateLandmarks = Arrays.asList(landmark1, landmark2);
        when(landmarkManager.getAllRouLandmark()).thenReturn(candidateLandmarks);
        when(gameDataRepository.findLandmarkById("landmark1")).thenReturn(Optional.of(landmark1));
        when(gameDataRepository.findLandmarkById("landmark2")).thenReturn(Optional.of(landmark2));
        
        // Start new game
        gameSession.startNewRound(100.0);
        
        // Verify initial state
        assertNotNull(gameSession.getCurrentTarget());
        assertEquals(2, gameSession.getUnsolvedLandmarks().size());
        assertFalse(gameSession.isGameFinished());

        // Simulate correct answer for first landmark
        when(playerState.getDetectedLandmark()).thenReturn(landmark2); // Current target is landmark2
        boolean result1 = gameSession.submitCurrentAnswer();
        
        // Verify first landmark is correctly answered
        assertTrue(result1);
        assertEquals(1, gameSession.getUnsolvedLandmarks().size());
        assertFalse(gameSession.getUnsolvedLandmarks().containsKey("landmark2"));

        // Simulate correct answer for second landmark
        when(playerState.getDetectedLandmark()).thenReturn(landmark1); 
        boolean result2 = gameSession.submitCurrentAnswer();
        
        // Verify second landmark is correctly answered, game completed
        assertTrue(result2);
        assertEquals(0, gameSession.getUnsolvedLandmarks().size());
        assertTrue(gameSession.isGameFinished());
    }

    /**
     * Test Scenario 2: Can automatically update target pool after 3 wrong answers and switch to next landmark
     * Verify that after 3 wrong answers, it automatically switches to the next landmark
     */
    @Test
    void scenario2() {
        // Prepare test data - 2 landmarks
        List<Landmark> candidateLandmarks = Arrays.asList(landmark1, landmark2);
        when(landmarkManager.getAllRouLandmark()).thenReturn(candidateLandmarks);
        when(gameDataRepository.findLandmarkById("landmark1")).thenReturn(Optional.of(landmark1));
        when(gameDataRepository.findLandmarkById("landmark2")).thenReturn(Optional.of(landmark2));
        
        // Start new game
        gameSession.startNewRound(100.0);
        
        // Verify initial state
        assertNotNull(gameSession.getCurrentTarget());
        assertEquals(2, gameSession.getUnsolvedLandmarks().size());
        assertFalse(gameSession.isGameFinished());

        // First wrong answer - no landmark detected
        when(playerState.getDetectedLandmark()).thenReturn(null);
        boolean result1 = gameSession.submitCurrentAnswer();
        assertFalse(result1);
        assertEquals(1, gameSession.getUnsolvedLandmarks().get(gameSession.getCurrentTarget().getId()));

        // Second wrong answer - wrong landmark detected
        String currentTargetId = gameSession.getCurrentTarget().getId();
        Landmark wrongLandmark = currentTargetId.equals("landmark1") ? landmark2 : landmark1;
        when(playerState.getDetectedLandmark()).thenReturn(wrongLandmark);
        boolean result2 = gameSession.submitCurrentAnswer();
        assertFalse(result2);
        assertEquals(2, gameSession.getUnsolvedLandmarks().get(currentTargetId));

        // Third wrong answer - no landmark detected again
        when(playerState.getDetectedLandmark()).thenReturn(null);
        boolean result3 = gameSession.submitCurrentAnswer();
        assertFalse(result3);
        
        // Debug information
        System.out.println("[DEBUG] After 3rd wrong answer:");
        System.out.println("[DEBUG] - targetPool size: " + gameSession.getUnsolvedLandmarks().size());
        System.out.println("[DEBUG] - targetPool contents: " + gameSession.getUnsolvedLandmarks().keySet());
        System.out.println("[DEBUG] - current target: " + (gameSession.getCurrentTarget() != null ? gameSession.getCurrentTarget().getId() : "null"));
        
        // Verify that after reaching max wrong answers, current landmark is removed and next landmark is automatically selected
        assertEquals(1, gameSession.getUnsolvedLandmarks().size());
        assertNotNull(gameSession.getCurrentTarget()); // Should automatically select next landmark
        assertFalse(gameSession.isGameFinished());
    }

    /**
     * Test Scenario 3: If first landmark is intentionally answered wrong, will it trigger automatic new game restart
     * Verify that when there's only one landmark, after 3 wrong answers the game ends
     */
    @Test
    void scenario3() {
        // Prepare test data - only 1 landmark
        List<Landmark> candidateLandmarks = Arrays.asList(landmark1);
        when(landmarkManager.getAllRouLandmark()).thenReturn(candidateLandmarks);
        when(gameDataRepository.findLandmarkById("landmark1")).thenReturn(Optional.of(landmark1));
        
        // Start new game
        gameSession.startNewRound(100.0);
        
        // Verify initial state
        assertNotNull(gameSession.getCurrentTarget());
        assertEquals(1, gameSession.getUnsolvedLandmarks().size());
        assertFalse(gameSession.isGameFinished());

        // First wrong answer
        when(playerState.getDetectedLandmark()).thenReturn(null);
        boolean result1 = gameSession.submitCurrentAnswer();
        assertFalse(result1);
        assertEquals(1, gameSession.getUnsolvedLandmarks().get(gameSession.getCurrentTarget().getId()));

        // Second wrong answer
        when(playerState.getDetectedLandmark()).thenReturn(null);
        boolean result2 = gameSession.submitCurrentAnswer();
        assertFalse(result2);
        assertEquals(2, gameSession.getUnsolvedLandmarks().get(gameSession.getCurrentTarget().getId()));

        // Third wrong answer
        when(playerState.getDetectedLandmark()).thenReturn(null);
        boolean result3 = gameSession.submitCurrentAnswer();
        assertFalse(result3);
        
        // Verify that after reaching max wrong answers, since there are no more landmarks, the game ends
        assertEquals(0, gameSession.getUnsolvedLandmarks().size());
        assertTrue(gameSession.isGameFinished());
    }

    /**
     * Test edge case: Detected landmark not in target pool
     */
    @Test
    void testDetectedLandmarkNotInTargetPool() {
        // Prepare test data
        List<Landmark> candidateLandmarks = Arrays.asList(landmark1);
        when(landmarkManager.getAllRouLandmark()).thenReturn(candidateLandmarks);
        when(gameDataRepository.findLandmarkById("landmark1")).thenReturn(Optional.of(landmark1));
        
        // Start new game
        gameSession.startNewRound(100.0);
        
        // Simulate detecting a landmark not in the target pool
        when(playerState.getDetectedLandmark()).thenReturn(landmark2); // landmark2 is not in target pool
        boolean result = gameSession.submitCurrentAnswer();
        
        // Verify this is treated as a wrong answer
        assertFalse(result);
        assertEquals(1, gameSession.getUnsolvedLandmarks().get(gameSession.getCurrentTarget().getId()));
    }

    /**
     * Test player position update
     */
    @Test
    void testUpdatePlayerPosition() {
        // Verify player position update is correctly called
        gameSession.updatePlayerPosition(51.8950, -8.4910, 180.0);
        
        // Verify PlayerStateManager's updatePlayerPosition method is called
        verify(playerState).updatePlayerPosition(51.8950, -8.4910, 180.0);
    }

    /**
     * Test answer correctness check
     */
    @Test
    void testAnswerCorrectness() {
        // Prepare test data
        List<Landmark> candidateLandmarks = Arrays.asList(landmark1);
        when(landmarkManager.getAllRouLandmark()).thenReturn(candidateLandmarks);
        when(gameDataRepository.findLandmarkById("landmark1")).thenReturn(Optional.of(landmark1));
        
        // Start new game
        gameSession.startNewRound(100.0);
        Landmark currentTarget = gameSession.getCurrentTarget();
        
        // Set current detected landmark
        when(playerState.getDetectedLandmark()).thenReturn(currentTarget);
        
        // Test correct answer
        assertTrue(gameSession.answerCorrect(currentTarget));
        
        // Test wrong answer
        assertFalse(gameSession.answerCorrect(landmark2));
    }
}
