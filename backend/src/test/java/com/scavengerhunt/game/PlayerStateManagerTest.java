package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.GameDataRepository;

public class PlayerStateManagerTest {

    @Mock private LandmarkManager mockLandmarkManager;
    @Mock private GameDataRepository mockGameDataRepo;

    private Player mockPlayer;
    private PlayerStateManager testPlayerStateManager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockPlayer = new Player(51.8947, -8.4903, 0.0, "Cork");

        testPlayerStateManager = new PlayerStateManager(mockPlayer, mockLandmarkManager, mockGameDataRepo);
    }

    @Test
    void initialState_gameNotFinish() {
        assertFalse(testPlayerStateManager.isGameFinished(), "Game should not be finished on creation");
    }

    @Test
    void setGameFinished() {
        testPlayerStateManager.setGameFinished();
        assertTrue(testPlayerStateManager.isGameFinished());
    }

    @Test
    void resetGame_clearFinishedFlag() {
        testPlayerStateManager.setGameFinished();
        testPlayerStateManager.resetGame();
        assertFalse(testPlayerStateManager.isGameFinished(), "resetGame() should clear the finished flag");
    }

    @Test
    void updatePlayerPosition_updatesPlayer() {
        testPlayerStateManager.updatePlayerPosition(51.9000, -8.5000, 90.0);

        assertEquals(51.9000, testPlayerStateManager.getPlayer().getLatitude(), 0.0001);
        assertEquals(-8.5000, testPlayerStateManager.getPlayer().getLongitude(), 0.0001);
        assertEquals(90.0, testPlayerStateManager.getPlayer().getAngle(), 0.0001);
    }

    @Test
    void getPlayer_returnsSamePlayer() {
        assertSame(mockPlayer, testPlayerStateManager.getPlayer(), "getPlayer() should return the player passed in constructor");
    }

    // NOTE: updateDetectedLandmark() uses GeoUtils.detectedLandmark() with real polygon data.
    // That path is covered by integration/E2E tests, not unit tests.
}
