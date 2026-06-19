package com.scavengerhunt.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateRiddleRequest(
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("landmark_id") String landmarkId,
    @JsonProperty("difficulty") double difficulty,
    @JsonProperty("language") String language,
    @JsonProperty("style") String style,
    @JsonProperty("puzzle_pool") List<String> puzzlePool
) {}
