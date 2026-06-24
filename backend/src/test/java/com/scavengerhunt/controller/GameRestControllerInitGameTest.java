package com.scavengerhunt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scavengerhunt.client.LandmarkProcessorClient;
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

}
