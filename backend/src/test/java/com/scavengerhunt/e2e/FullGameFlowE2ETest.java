package com.scavengerhunt.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.security.JwtTokenProvider;
import com.scavengerhunt.service.GameSessionService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FullGameFlowE2ETest {

    @LocalServerPort int port;

    @Autowired JwtTokenProvider tokenProvider;
    @Autowired GameSessionService gameSessionService;
    @Autowired TestRestTemplate restTemplate;

    private String userId;
    private String token;
    private String base;

    @BeforeEach
    void setup(){
        userId = "e2e-" + System.currentTimeMillis();
        token = tokenProvider.generateToken(userId, "TestPlayer");
        base = "http://localhost:" + port;
        gameSessionService.removeSession(userId);
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void noToken_allGameEndpoints_returns401() {
        ResponseEntity<String> r1 = restTemplate.postForEntity(
            base + "/api/game/update-position",
            Map.of("latitude", 51.894, "longitude", -8.490, "angle", 0.0),
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, r1.getStatusCode());

        ResponseEntity<String> r2 = restTemplate.postForEntity(
            base + "/api/game/start-round",
            Map.of("latitude", 51.894, "longitude", -8.490, "angle", 0.0),
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, r2.getStatusCode());

        ResponseEntity<String> r3 = restTemplate.postForEntity(
            base + "/api/game/submit-answer",
            Map.of("secondsUsed", 500),
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, r3.getStatusCode());
    }

    @Test
    void noSession_startRound_returns404() {
        ResponseEntity<String> r = restTemplate.exchange(
            base + "/api/game/start-round",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("latitude", 51.894, "longitude", -8.490, "angle", 0.0,"radiusMeters", 500.0), headers()),
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
    }

    @Test
    void sessionLifecycle_initAndFinish() {
        ResponseEntity<?> r = restTemplate.exchange(
            base + "/api/game/init-game",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("latitude", 51.894, "longitude", -8.490, "angle", 0.0), headers()),
            Map.class
        );

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertTrue(gameSessionService.findByUserId(userId).isPresent());
    }

    @Test
    void finishedSession_startRound_returns400() {
        PersistedGameSession session = gameSessionService.createSession(userId, "Cork");
        session.setFinished(true);
        gameSessionService.save(session);

        ResponseEntity<String> r = restTemplate.exchange(
            base + "/api/game/start-round",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("radiusMeters", 500.0), headers()),
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }
}
