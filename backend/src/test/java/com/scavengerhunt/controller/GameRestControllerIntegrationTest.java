package com.scavengerhunt.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.security.JwtTokenProvider;
import com.scavengerhunt.service.GameSessionService;

@SpringBootTest
@AutoConfigureMockMvc
public class GameRestControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider tokenProvider;
    @Autowired GameSessionService gameSessionService;

    private String token;
    private String userId;

    @BeforeEach
    void setup(){
        userId = "game-test-" + System.currentTimeMillis();
        token = tokenProvider.generateToken(userId, "Player");
        gameSessionService.removeSession(userId);
    }

    // ========= Auth enforcement =========
    @Test
    void updatePosition_withoutToken_return401() throws Exception{
        mockMvc.perform(post("/api/game/update-position")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                Map.of(
                "latitude", 51.894, "longitude", -8.490, "angle", 0.0
                )
            ))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void startRound_withoutToken_return401() throws Exception{
        mockMvc.perform(post("/api/game/start-round")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("radiusMeters", 500))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void submitAnswer_withoutToken_return401() throws Exception{
        mockMvc.perform(post("/api/game/submit-answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("secondsUsed", 30))))
            .andExpect(status().isUnauthorized());
    }

    // ========= No session =========

    @Test
    void updatePosition_noSession_returns404() throws Exception {
        mockMvc.perform(post("/api/game/update-position")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("latitude", 51.894, "longitude", -8.490, "angle", 0.0)))
                .header("Authorization", "Bearer " + token)
            ).andExpect(status().isNotFound());
    }

    @Test
    void startRound_noSession_returns404() throws Exception {
        mockMvc.perform(post("/api/game/start-round")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("latitude", 51.894, "longitude", -8.490, "angle", 0.0, "radiusMeters", 500.0)
                    )
                ).header("Authorization", "Bearer " + token)
            ).andExpect(status().isNotFound());
    }

    @Test
    void submitAnswer_noSession_returns404() throws Exception {
        mockMvc.perform(post("/api/game/submit-answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("secondsUsed", 500)
                    )
                ).header("Authorization", "Bearer " + token)
            ).andExpect(status().isNotFound());
    }

    // ========= Session state =========

    @Test
    void startRound_gameAlreadyFinished_returns400() throws Exception {
        PersistedGameSession session = gameSessionService.createSession(userId, "Cork");
        session.setFinished(true);
        gameSessionService.save(session);

        mockMvc.perform(post("/api/game/start-round")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("radiusMeters", 500)))
                .header("Authorization", "Bearer " + token)
            ).andExpect(status().isBadRequest());
    }

    @Test
    void updatePosition_existingSession_returns200() throws Exception {
        PersistedGameSession session = gameSessionService.createSession(userId, "Cork");
        gameSessionService.save(session);

        mockMvc.perform(post("/api/game/update-position")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("latitude", 51.894, "longitude", -8.490, "angle", 45.0)))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void finishRound_withSession_returns200AndRemovesSession() throws Exception {
        gameSessionService.createSession(userId, "Cork");

        mockMvc.perform(post("/api/game/finish-round")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        assertTrue(gameSessionService.findByUserId(userId).isEmpty());
    }
}
