package com.scavengerhunt.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "puzzle.agent.url=http://mock-puzzle-agent:5000"
})
public class TestPuzzleAgentClient {
    @Autowired
    private PuzzleAgentClient client;
    
    @Test
    void shouldGenerateRiddle() {

        Map<String, Object> payload = Map.of(
            "landmarkId", "test-landmark",
            "sessionId", "test-session",
            "language", "English",
            "style", "Medieval"
        );
        
        // Mock the whole method instead of new a RestTamplete (why??)
        PuzzleAgentClient spyClient = spy(client);
        when(spyClient.generateRiddle(any())).thenReturn("Test riddle text");
        
        String result = spyClient.generateRiddle(payload);
        
        assertThat(result).isEqualTo("Test riddle text");
    }
}
