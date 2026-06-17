package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.AnswerTransactionRecordRepository;
import com.scavengerhunt.repository.GameDataRepository;

public class GameLogicManagerTest {

    @Mock private GameDataRepository mockGameDataRepo;
    @Mock private LandmarkProcessorClient mockLandmarkProcessorClient;
    @Mock PuzzleAgentClient mockPuzzleAgentClient;
    @Mock PlayerStateManager mockPlayerStateManager;

    @Mock AnswerTransactionRecordRepository mockAnswerTransactionRecordRepo;

    private PersistedGameSession mockSession;
    private User mockUser;

    //test landmarks
    private Landmark glucksman = new Landmark(
        "id-glucksman",
        "Glucksman Gallery",
        "Cork",
        51.894741757894735,
        -8.490317963157894);

    Landmark quad = new Landmark(
        "id-quad",
        "The Quad",
        "Cork",
        51.89372202222222,
        -8.492224097916667);

    Landmark boole = new Landmark(
        "id-boole",
        "Boole Library",
        "Cork",
        51.89285984,
        -8.491245088);

    @BeforeEach
    public void setup() {

        MockitoAnnotations.openMocks(this);

        mockSession = new PersistedGameSession("mock-session-123", "id-alex", "Cork");
        mockUser = new User("user-alex","mock-pw");
        mockUser.setUserId("id-alex");

        when(mockGameDataRepo.findLandmarkById("id-glucksman")).thenReturn(glucksman);
        when(mockGameDataRepo.findLandmarkById("id-quad")).thenReturn(quad);
        when(mockGameDataRepo.findLandmarkById("id-boole")).thenReturn(boole);
        when(mockGameDataRepo.getUserById("id-alex")).thenReturn(mockUser);

        when(mockPlayerStateManager.isGameFinished()).thenReturn(false);
        when(mockPlayerStateManager.getPlayer()).thenReturn(
            new Player(51.8947, -8.4903, 0.0, "Cork")
        );
    }

    private GameLogicManager buildGame(Map<String, Integer> pool, String currentTargetId, Landmark detectedLandmark){

        mockSession.setAttemptsByLandmarkId(pool);
        mockSession.setCurrentTargetId(currentTargetId);
        when(mockPlayerStateManager.getDetectedLandmark()).thenReturn(detectedLandmark);
        return new GameLogicManager(mockSession, mockGameDataRepo, mockPlayerStateManager, mockPuzzleAgentClient, mockAnswerTransactionRecordRepo, 20);
    }

    // ============ Correct Answer ============
    @Test
    void correctAnswer_removesLandmarkFromPool(){
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3, "id-quad", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        boolean res = game.submitCurrentAnswer(60);

        assertTrue(res);
        assertFalse(mockSession.getAttemptsByLandmarkId().containsKey("id-glucksman"), "Correct answer should be removed from the pool");
    }

    @Test
    void correctAnswer_advancedToNextTarget(){
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3, "id-quad", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        game.submitCurrentAnswer(60);

        assertNotEquals(mockSession.getCurrentTargetId(), "id-glucksman", "CurrentTarget should be the quad");

        assertEquals(mockSession.getCurrentTargetId(), "id-quad", "CurrentTarget should be the quad");

        assertNotNull(mockSession.getCurrentTargetId());
    }

    @Test
    void correctAnswer_lastLandmark_gameFinished(){
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        game.submitCurrentAnswer(60);

        assertTrue(mockSession.isFinished());
        assertNull(mockSession.getCurrentTargetId());
    }

    // ============ Wrong Answer ============
    @Test
    void wrongAnswer_decrementsAttemptCount(){
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3,
        "id-quad", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", quad);

        game.submitCurrentAnswer(60);

        assertEquals(2, mockSession.getAttemptsByLandmarkId().get("id-glucksman"), "Wrong answer should decrement attempts from 3 to 2");
    }

    @Test
    void wrongAnswer_zeroAttempts_removeCurrentLmFromPool(){
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 1,
        "id-quad", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", null); // not detecting any landmarks

        game.submitCurrentAnswer(60);

        assertFalse(mockSession.getAttemptsByLandmarkId().containsKey("id-glucksman"), "Attempts reduced to 0, it should be removed from the pool");
        assertEquals(mockSession.getCurrentTargetId(), "id-quad", "Next target after last attempt failed should be quad");
    }

    @Test
    void wrongAnswer_lastLandmark_zeroAttempts_gameFinished(){
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 1));
        GameLogicManager game = buildGame(pool, "id-glucksman", null);

        game.submitCurrentAnswer(60);

        assertTrue(mockSession.isFinished());
    }

    // ============ Time Expired ============
    @Test
    void timeExpired_treatedAsWrongAnswer() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        // 30 minutes * 60 seconds = 1800 = time limit
        boolean result = game.submitCurrentAnswer(1800);

        assertFalse(result, "Expired time should return false");
        assertEquals(2, mockSession.getAttemptsByLandmarkId().get("id-glucksman"),
            "Time expired should decrement attempts");
    }

    // ======= Guard: no current target =======
    @Test
    void submitAnswer_noCurrentTarget_returnsFalse() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        mockSession.setAttemptsByLandmarkId(pool);
        mockSession.setCurrentTargetId(null); // no target set
        when(mockPlayerStateManager.getDetectedLandmark()).thenReturn(glucksman);

        GameLogicManager game = new GameLogicManager(
            mockSession, mockGameDataRepo, mockPlayerStateManager,
            mockPuzzleAgentClient, mockAnswerTransactionRecordRepo, 30
        );

        boolean result = game.submitCurrentAnswer(60);
        assertFalse(result, "No current target should return false immediately");
    }

    // ============ basic functions ============

    @Test
    void updatePlayerPosition_updatesSession() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        game.updatePlayerPosition(51.895, -8.488, 90.0);

        assertEquals(51.895, mockSession.getPlayerLat(), 0.0001);
        assertEquals(-8.488, mockSession.getPlayerLng(), 0.0001);
        assertEquals(90.0, mockSession.getPlayerAngle(), 0.0001);
    }

    @Test
    void isGameFinished_returnsFalse_whenPoolNotEmpty() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        assertFalse(game.isGameFinished());
    }

    @Test
    void isGameFinished_returnsTrue_whenPoolEmpty() {
        Map<String, Integer> pool = new HashMap<>();
        GameLogicManager game = buildGame(pool, null, null);

        assertTrue(game.isGameFinished());
    }

    @Test
    void getCurrentTarget_returnsTargetMap_withCorrectFields() {
        glucksman.setRiddle("A riddle about art.");
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        Map<String, Object> target = game.getCurrentTarget();

        assertNotNull(target);
        assertEquals("id-glucksman", target.get("id"));
        assertEquals("Glucksman Gallery", target.get("name"));
        assertEquals(3, target.get("attemptsLeft"));
        assertEquals("A riddle about art.", target.get("riddle"));
    }

    @Test
    void getCurrentTarget_returnsNull_whenGameFinished() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        when(mockPlayerStateManager.isGameFinished()).thenReturn(true);
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        assertNull(game.getCurrentTarget());
    }

    @Test
    void answerCorrect_returnsTrue_whenDetectedMatchesTarget() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", glucksman);

        assertTrue(game.answerCorrect(glucksman));
    }

    @Test
    void answerCorrect_returnsFalse_whenDetectedDiffersFromTarget() {
        Map<String, Integer> pool = new HashMap<>(Map.of("id-glucksman", 3));
        GameLogicManager game = buildGame(pool, "id-glucksman", quad);

        assertFalse(game.answerCorrect(quad));
    }
}
