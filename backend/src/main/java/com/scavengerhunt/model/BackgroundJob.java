package com.scavengerhunt.model;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "background_job")
public class BackgroundJob {

    public enum Status{ PENDING, IN_PROGRESS, DONE, FAILED}

    @Id private String jobId;

    private String type;
    private Map<String, Object> payload;

    @Indexed
    private Status status;

    private int attemptCount;
    private String idempotencyKey;  // prevents duplicate jobs for the same city
    private String lastError;

    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public static BackgroundJob fetchLandmarks(String city, double lat, double lng) {
        BackgroundJob job = new BackgroundJob();
        job.jobId = java.util.UUID.randomUUID().toString();
        job.type = "FETCH_LANDMARKS";
        job.payload = Map.of("city", city, "latitude", lat, "longitude", lng);
        job.status = Status.PENDING;
        job.idempotencyKey = "fetch-landmarks-" + city.toLowerCase().replaceAll("\\s+", "-");
        job.createdAt = Instant.now();
        job.attemptCount = 0;
        return job;
    }

    // --- Getters and Setters ---

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

}
