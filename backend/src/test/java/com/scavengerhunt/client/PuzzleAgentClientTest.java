package com.scavengerhunt.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.scavengerhunt.client.dto.GenerateRiddleRequest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
class PuzzleAgentClientTest {

    private static MockWebServer server;

    @Autowired
    private PuzzleAgentClient client;

    @DynamicPropertySource
    static void puzzleAgentProperties(DynamicPropertyRegistry registry) throws IOException {
        server = new MockWebServer();
        server.start();
        registry.add("app.puzzle-agent.url", () -> server.url("/").toString());
    }

    @Test
    void generateRiddle_returnsRiddleFromResponse() {
        server.enqueue(new MockResponse()
            .setBody("""
                {"status":"ok","riddle":"Find the tall stone building","landmark_id":"lm1","session_id":"s1"}
                """)
            .addHeader("Content-Type", "application/json"));

        var req = new GenerateRiddleRequest("s1", "lm1", 50.0, "English", "Medieval", null);
        String riddle = client.generateRiddle(req);
        assertEquals("Find the tall stone building", riddle);
    }

    @Test
    void generateRiddle_returnsFallbackOn500() {
        server.enqueue(new MockResponse().setResponseCode(500));

        var req = new GenerateRiddleRequest("s1", "lm1", 50.0, "English", "Medieval", null);
        String riddle = client.generateRiddle(req);
        assertNotNull(riddle);
        assertEquals("Find the landmark that matches your target. Look carefully at the surroundings.", riddle);
    }
}
