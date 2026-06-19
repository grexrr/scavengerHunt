package com.scavengerhunt.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateRiddleResponse(
    @JsonProperty("status") String status,
    @JsonProperty("riddle") String riddle,
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("landmark_id") String landmarkId
) {}
