package com.scavengerhunt.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.scavengerhunt.model.Landmark;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class LandmarkProcessorClientTest {

    private MockWebServer mockServer;
    private LandmarkProcessorClient client;

    @BeforeEach
    void setup() throws IOException {
        mockServer = new MockWebServer();
        client = new LandmarkProcessorClient(
            RestClient.builder()
                .baseUrl(mockServer.url("/").toString())
                .build()
        );
    }

    @AfterEach
    void teardown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void resolveCity_success_returnCityName() throws InterruptedException {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"ok\",\"city\":\"Cork\"}")
            .setHeader("Content-Type", "application/json"));
        String city = client.resolveCity(51.8936, -8.4920);
        assertEquals("Cork", city);
    }

    @Test
    void resolveCity_sendsCorrectJsonFields() throws InterruptedException {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"ok\",\"city\":\"Cork\"}")
            .addHeader("Content-Type", "application/json"));

        client.resolveCity(51.8936, -8.4920);
        RecordedRequest req = mockServer.takeRequest();
        String body = req.getBody().readUtf8();

        assertTrue(body.contains("\"latitude\""), "Must send 'latitude' key, got: " + body);
        assertTrue(body.contains("\"longitude\""), "Must send 'longitude' key, got: " + body);
        assertEquals("/resolve-city", req.getPath());
    }

    @Test
    void resolveCity_4xxResponse_returnsNull() {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"code\":\"MISSING_COORDINATES\",\"message\":\"latitude and longitude are required\"}")
            .addHeader("Content-Type", "application/json"));

            String city = client.resolveCity(51.8936, -8.4920);

        assertNull(city, "4xx response should return null, not throw");
    }

    @Test
    void resolveCity_emptyCityInResponse_returnsNull() throws InterruptedException {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"ok\",\"city\":\"\"}")
            .addHeader("Content-Type", "application/json"));

        String city = client.resolveCity(51.8936, -8.4920);

        assertNull(city, "Empty city string should return null");
    }

    @Test
    void ensureLandmarkMeta_sendsLandmarkIdsKey() throws InterruptedException {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"ok\",\"generated\":2,\"skipped\":0,\"failed\":0}")
            .addHeader("Content-Type", "application/json"));

        Landmark lm1 = new Landmark();
        lm1.setId("lm-001");
        Landmark lm2 = new Landmark();
        lm2.setId("lm-002");

        client.ensureLandmarkMeta(List.of(lm1, lm2));

        RecordedRequest req = mockServer.takeRequest();
        String body = req.getBody().readUtf8();

        assertTrue(body.contains("\"landmark_ids\""), "Must send 'landmark_ids' (snake_case) key, got: " + body);
        assertEquals("/generate-landmark-meta", req.getPath());
    }

    @Test
    void ensureLandmarkMeta_500Response_doesNotThrow() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        Landmark lm = new Landmark();
        lm.setId("lm-001");

        assertDoesNotThrow(() -> client.ensureLandmarkMeta(List.of(lm)), "500 from Python service should be caught and logged, not thrown");
    }

    @Test
    void ensureLandmarkMeta_emptyList_doesNotCallServer() throws InterruptedException {
        client.ensureLandmarkMeta(List.of());

        assertEquals(0, mockServer.getRequestCount(), "Empty list should not call the server");
    }
}
