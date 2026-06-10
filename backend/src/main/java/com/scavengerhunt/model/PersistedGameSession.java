package com.scavengerhunt.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "game_sessions")
public class PersistedGameSession {

    @Id
    private String sessionId;

    @Indexed
    private String userId;

    private String city;
    private double playerLat;
    private double playerLng;
    private double playerAngle;

    private Map<String, Integer> attemptsByLandmarkId;
    private List<String> solvedLandmarkIds;
    private String currentTargetId;

    private boolean finished;

    @Indexed(expireAfterSeconds = 7200)  // sessions auto-deleted after 2 hours of inactivity
    private Instant lastUpdated;

    @Version
    private Long version;   // optimistic locking — prevents two concurrent writes from clobbering each other

    private PersistedGameSession() {}

    public PersistedGameSession(String sessionId, String userId, String city) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.city = city;
        this.finished = false;
        this.lastUpdated = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getPlayerLat() { return playerLat; }
    public void setPlayerLat(double playerLat) { this.playerLat = playerLat; }

    public double getPlayerLng() { return playerLng; }
    public void setPlayerLng(double playerLng) { this.playerLng = playerLng; }

    public double getPlayerAngle() { return playerAngle; }
    public void setPlayerAngle(double playerAngle) { this.playerAngle = playerAngle; }

    public Map<String, Integer> getAttemptsByLandmarkId() { return this.attemptsByLandmarkId; }
    public void setAttemptsByLandmarkId(Map<String, Integer> m) { this.attemptsByLandmarkId = m; }

    public List<String> getSolvedLandmarkIds() { return solvedLandmarkIds; }
    public void setSolvedLandmarkIds(List<String> ids) { this.solvedLandmarkIds = ids; }

    public String getCurrentTargetId() { return currentTargetId; }
    public void setCurrentTargetId(String id) { this.currentTargetId = id; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant t) { this.lastUpdated = t; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
