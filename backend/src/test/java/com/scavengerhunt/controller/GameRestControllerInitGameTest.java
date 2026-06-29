package com.scavengerhunt.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.LandmarkRepository;
import com.scavengerhunt.security.JwtTokenProvider;
import com.scavengerhunt.service.GameSessionService;
import com.scavengerhunt.service.JobCoordinator;

@SpringBootTest
@AutoConfigureMockMvc
public class GameRestControllerInitGameTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider tokenProvider;
    @Autowired GameSessionService gameSessionService;
    @Autowired LandmarkRepository landmarkRepo;

    @MockitoBean LandmarkProcessorClient landmarkProcessorClient;
    @MockitoBean JobCoordinator jobCoordinator;

    private String token;
    private String userId;

    private static final Map<String, Object> CORK_POSITION = Map.of("latitude", 51.8943, "longitude", -8.4922, "angle", 0.0);

    @BeforeEach
    void setup() {
        userId = "init-test-" + System.currentTimeMillis();
        token = tokenProvider.generateToken(userId, "PLAYER");
        gameSessionService.removeSession(userId);
        landmarkRepo.deleteAll();
    }

    @Test
    void initGame_cityResolutionFails_returns400() throws Exception {
        when(landmarkProcessorClient.resolveCity(anyDouble(), anyDouble())).thenReturn(null);

        mockMvc.perform(post("/api/game/init-game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CORK_POSITION))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.message").value("Could not resolve city from coordinates"));
    }

    @Test
    void initGame_fewLandmarks_returns202AndEnqueuesJob() throws Exception {
        when(landmarkProcessorClient.resolveCity(anyDouble(), anyDouble())).thenReturn("Cork");
        for (int i = 0; i < 5; i++){
            Landmark lm = new Landmark();
            lm.setId("lm-" + i);
            lm.setName("Landmark " + i);
            lm.setCity("Cork");
            lm.setGeometry(null);
            landmarkRepo.save(lm);
        }

        mockMvc.perform(post("/api/game/init-game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CORK_POSITION))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("PREPARING"))
            .andExpect(jsonPath("$.city").value("Cork"));

        verify(jobCoordinator).enqueueFetchLandmarks("Cork",anyDouble(), anyDouble());
    }

    @Test
    void initGame_noLandmarks_returns202AndEnqueuesJob() throws Exception  {
        when(landmarkProcessorClient.resolveCity(anyDouble(), anyDouble())).thenReturn("Dublin");

        mockMvc.perform(post("/api/game/init-game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CORK_POSITION))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isAccepted());

        verify(jobCoordinator).enqueueFetchLandmarks("Dublin", anyDouble(), anyDouble());
    }

    @Test
    void initGame_enoughLandmark_returns200WithLandmarks() throws Exception {
        when(landmarkProcessorClient.resolveCity(anyDouble(), anyDouble())).thenReturn("Cork");

        for (int i = 0; i < 10; i++){
            Landmark lm = new Landmark();
            lm.setId("lm-" + i);
            lm.setName("Landmark " + i);
            lm.setCity("Cork");

            GeoJsonPolygon polygon = new GeoJsonPolygon(
                new Point(0, 0),
                new Point(1, 0),
                new Point(1, 1),
                new Point(0, 0)
            );
            lm.setGeometry(polygon);
            lm.setCentroid(Map.of("latitude", 0.5, "longitude", 0.5));
            landmarkRepo.save(lm);
        }

        mockMvc.perform(post("/api/game/init-game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CORK_POSITION))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.landmarks").isArray())
            .andExpect(jsonPath("$.landmarks.length()").value(10));

            verify(jobCoordinator, never()).enqueueFetchLandmarks(any(), anyDouble(), anyDouble());
    }

    @Test
    void initGame_enoughLandmark_createSession() throws Exception {
        when(landmarkProcessorClient.resolveCity(anyDouble(), anyDouble())).thenReturn("Cork");

        for (int i = 0; i < 10; i++){
            Landmark lm = new Landmark();
            lm.setId("lm-" + i);
            lm.setName("Landmark " + i);
            lm.setCity("Cork");

            GeoJsonPolygon polygon = new GeoJsonPolygon(
                new Point(0, 0),
                new Point(1, 0),
                new Point(1, 1),
                new Point(0, 0)
            );
            lm.setGeometry(polygon);
            lm.setCentroid(Map.of("latitude", 0.5, "longitude", 0.5));
            landmarkRepo.save(lm);
        }

        mockMvc.perform(post("/api/game/init-game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CORK_POSITION))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

            assertTrue(gameSessionService.findByUserId(userId).isPresent());
    }

    //========== Auth Guard ==========
    @Test
    void initGame_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/game/init-game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CORK_POSITION)))
            .andExpect(status().isUnauthorized());
    }
}
