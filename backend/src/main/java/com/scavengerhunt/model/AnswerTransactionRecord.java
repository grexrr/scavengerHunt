package com.scavengerhunt.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "answer_transaction_records")
public class AnswerTransactionRecord {
    @Id private String id;

    @Indexed private String sessionId;
    @Indexed private String userId;
    @Indexed private String landmarkId;

    private boolean isCorrect;
    private int attemptCount;
    private long riddleDurationSeconds;

    private LocalDateTime createdAt;

    public static AnswerTransactionRecord forAttempt(
        String sessionId, String userId, String landmarkId,
        boolean isCorrect, int attemptCount, long durationSeconds
    ) {
        AnswerTransactionRecord r = new AnswerTransactionRecord();
        r.id = UUID.randomUUID().toString();
        r.sessionId = sessionId;
        r.userId = userId;
        r.landmarkId = landmarkId;
        r.isCorrect = isCorrect;
        r.attemptCount = attemptCount;
        r.riddleDurationSeconds = durationSeconds;
        r.createdAt = LocalDateTime.now();
        return r;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLandmarkId() {
        return landmarkId;
    }

    public void setLandmarkId(String landmarkId) {
        this.landmarkId = landmarkId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public long getRiddleDurationSeconds() {
        return riddleDurationSeconds;
    }

    public void setRiddleDurationSeconds(long riddleDurationSeconds) {
        this.riddleDurationSeconds = riddleDurationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
