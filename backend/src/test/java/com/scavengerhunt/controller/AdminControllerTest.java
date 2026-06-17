package com.scavengerhunt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.scavengerhunt.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider tokenProvider;

    @Test
    void insertLandmarks_withValidToken_returns200() throws Exception {
        String token = tokenProvider.generateToken("admin-user", "ADMIN");
        mockMvc.perform(post("/api/admin/insert-landmarks")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void insertUsers_withValidToken_returns200() throws Exception {
        String token = tokenProvider.generateToken("admin-user", "ADMIN");
        mockMvc.perform(post("/api/admin/insert-users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void clearLandmarks_withValidToken_returns200() throws Exception {
        String token = tokenProvider.generateToken("admin-user", "ADMIN");
        mockMvc.perform(delete("/api/admin/clear-landmarks")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void clearUsers_withValidToken_returns200() throws Exception {
        String token = tokenProvider.generateToken("admin-user", "ADMIN");
        mockMvc.perform(delete("/api/admin/clear-users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void adminEndpoints_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/insert-landmarks"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/admin/insert-users"))
            .andExpect(status().isUnauthorized());
    }
}
