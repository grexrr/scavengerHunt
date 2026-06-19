package com.scavengerhunt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.scavengerhunt.client.dto.GenerateRiddleRequest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class PuzzleAgentClientTest {

    private MockWebServer server;
    private PuzzleAgentClient client;

    @BeforeEach
    void setup() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new PuzzleAgentClient(server.url("/").toString(), 5);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
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
        assertEquals(riddle, "Find the tall stone building");
    }

    @Test
    void generateRiddle_returnsFallbackOn500() {
        server.enqueue(new MockResponse().setResponseCode(500));

        var req = new GenerateRiddleRequest("s1", "lm1", 50.0, "English", "Medieval", null);

        String riddle = client.generateRiddle(req);
        assertNotNull(riddle);
        assertEquals(riddle, "Find the landmark that matches your target. Look carefully at the surroundings.");
    }
}
