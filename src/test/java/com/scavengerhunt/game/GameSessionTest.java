package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.Player;

public class GameSessionTest {

    private GameSession gameSession;
    private PlayerStateManager mockPlayerState;
    private LandmarkManager mockLandmarkManager;

    @BeforeEach
    public void setUp() {
        mockPlayerState = Mockito.mock(PlayerStateManager.class);
        mockLandmarkManager = Mockito.mock(LandmarkManager.class);

        // Mock player state
        Player mockPlayer = Mockito.mock(Player.class);
        Mockito.when(mockPlayer.getLatitude()).thenReturn(51.0);
        Mockito.when(mockPlayer.getLongitude()).thenReturn(-8.5);
        Mockito.when(mockPlayer.getAngle()).thenReturn(90.0);
        Mockito.when(mockPlayerState.getPlayer()).thenReturn(mockPlayer);


        Landmark landmark1 = new Landmark(1, "Glucksman Gallery", "Where art hides behind glass and stone", 51.8947384, -8.4903073);
        Landmark landmark2 = new Landmark(2, "Honan Chapel", "Echoes of vows and silent prayer linger here", 51.8935836, -8.49002395);
        List<Landmark> landmarks = Arrays.asList(landmark1, landmark2);
        Mockito.when(mockLandmarkManager.getLocalLandmarksWithinRadius(51.0, -8.5, 2000))
               .thenReturn(landmarks);

        gameSession = new GameSession(mockPlayerState, mockLandmarkManager);
    }

    @Test
    public void testStartNewRound() {
        gameSession.startNewRound(2000);

        // Verify player state reset
        Mockito.verify(mockPlayerState).resetPlayerTo(51.0, -8.5, 90.0);

        // Verify current target pool is set correctly
        assertEquals(2, gameSession.getUnsolvedLandmarks().size());
    }
}
