package com.scavengerhunt.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_round_records")
public class GameRoundRecord {
    @Id
    private String id;

    private String userId;
    private String landmarkId;

    private LocalDateTime roundStartTime;
    private LocalDateTime riddleStarTime;
    private LocalDateTime riddleEndTime;
    private long riddleDurationSeconds;
    private LocalDateTime roundEndTime;

    private boolean isCorrect;
    private int attemptCount;
    private boolean isTimeExpired;

    private double userRatingAfter;
    private double landmarkRatingAfter;

    private LocalDateTime createAt;
}
