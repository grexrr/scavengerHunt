package com.scavengerhunt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.scavengerhunt.model.PersistedGameSession;

@SpringBootTest
public class GameSessionServiceIntegrationTest {

    @Autowired private GameSessionService gameSessionService;

    private final String userId = "integration-test-user";

    @BeforeEach
    void cleanup(){
        gameSessionService.removeSession(userId);
    }

    @Test
    void createSession_persistsToDb(){
        PersistedGameSession mockSession = gameSessionService.createSession(userId, "Cork");

        assertNotNull(mockSession.getSessionId());
        assertEquals(userId, mockSession.getUserId());
        assertEquals("Cork", mockSession.getCity());
        assertTrue(gameSessionService.findByUserId(userId).isPresent());
    }

    @Test
    void createSession_replaceExistingSession(){
        gameSessionService.createSession(userId, "Cork");
        PersistedGameSession second = gameSessionService.createSession(userId, "Dublin");

        assertEquals("Dublin", second.getCity());

        assertTrue(gameSessionService.findByUserId(userId).isPresent());
    }

    @Test
    void findByUserId_noSession_returnsEmpty(){
        assertTrue(gameSessionService.findByUserId("nobody").isEmpty());
    }

    @Test
    void save_persistsChanges() {
        PersistedGameSession session = gameSessionService.createSession(userId, "Cork");
        session.setFinished(true);
        gameSessionService.save(session);

        PersistedGameSession reloaded = gameSessionService.findByUserId(userId).get();

        assertTrue(reloaded.isFinished());
    }

    @Test
    void removeSession_deletesFromDb(){
        gameSessionService.createSession(userId, "Cork");
        gameSessionService.removeSession(userId);

        assertTrue(gameSessionService.findByUserId(userId).isEmpty());
    }

    @Test
    void hasSession_trueAfterCreate_falseAfterRemove(){
        assertFalse(gameSessionService.hasSession(userId));
        gameSessionService.createSession(userId, "Cork");
        assertTrue(gameSessionService.hasSession(userId));
        gameSessionService.removeSession(userId);
        assertFalse(gameSessionService.hasSession(userId));
    }
}
