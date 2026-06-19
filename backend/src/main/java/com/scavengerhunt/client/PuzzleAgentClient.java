package com.scavengerhunt.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.scavengerhunt.client.dto.GenerateRiddleRequest;
import com.scavengerhunt.client.dto.GenerateRiddleResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class PuzzleAgentClient {

    private static final Logger log = LoggerFactory.getLogger(PuzzleAgentClient.class);

    private final RestClient restClient;


    public PuzzleAgentClient(
        @Value("${app.puzzle-agent.url}") String baseUrl,
        @Value("${app.puzzle-agent.timeout-seconds}") int timeoutSeconds
    ){
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);

        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();
    }

    @CircuitBreaker(name = "puzzleAgent", fallbackMethod = "riddleFallback")
    public String generateRiddle(GenerateRiddleRequest req)  {
        var resp = restClient.post()
            .uri("/generate-riddle")
            .body(req)
            .retrieve()
            .body(GenerateRiddleResponse.class);

        if (resp == null || resp.riddle() == null){
            throw new RuntimeException("Empty riddle response from Puzzle Agent");
        }
        return resp.riddle();
    }

    private String riddleFallback(GenerateRiddleRequest req, Throwable t) {
        log.warn("Puzzle Agent unavailable for landmark {}, using fallback. Cause: {}", req.landmarkId(), t.getMessage());
        return "Find the landmark that matches your target. Look carefully at the surroundings.";
    }

    public void resetSession(String sessionId) {
        restClient.post()
        .uri("/reset-session")
        .body(Map.of("session_id", sessionId))
        .retrieve()
        .toBodilessEntity();
    }
}
