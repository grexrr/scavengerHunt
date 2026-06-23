package com.scavengerhunt.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

public class LandmarkIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LandmarkIngestionService.class);
    private final RestClient restClient;

    public LandmarkIngestionService(
        @Value("${app.landmark-processor.url:http://landmark-processor:5000}") String baseUrl
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void fetchForLocation(double lat, double lng) {
        restClient.post()
            .uri("/fetch-landmark")
            .body(Map.of("latitude", lat, "longitude", lng))
            .retrieve()
            .toBodilessEntity();
        log.info("Landmark fetch triggered for {},{}", lat, lng);
    }
}
