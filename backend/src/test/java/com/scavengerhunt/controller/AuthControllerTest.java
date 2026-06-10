package com.scavengerhunt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scavengerhunt.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider tokenProvider;

    @Test
    void registerEndpoint_successWithUsername() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "mockUserName", "password", "password")
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());    // 200 OK
    }

    @Test
    void registerEndpoint_duplicatedUsername() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "duplicateUser", "password", "password")
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict());    // 409 OK
    }

    @Test
    void loginEndpoint_isPublic() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "nonexistent", "password", "wrong")
        );
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNotFound());    // 404 user not found
    }

    @Test
    void logoutEndpoint_userSuccessWithToken() throws Exception {
        String userId = "test-user-id";
        String token = tokenProvider.generateToken(userId, "PLAYER");
        mockMvc.perform(post("/api/auth/logout")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());    // 200
    }

    @Test
    void logoutEndpoint_requiresToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        ).andExpect(status().isUnauthorized()); // 401 unauthorized
    }

    @Test
    void getProfileEndpoint_notFoundForUnknownUser() throws Exception {
        String userId = "test-user-id";
        String token = tokenProvider.generateToken(userId, "PLAYER");
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    void getProfileEndpoint_requiresToken() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
            .andExpect(status().isUnauthorized());
    }

}
