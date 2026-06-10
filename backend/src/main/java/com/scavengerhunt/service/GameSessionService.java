package com.scavengerhunt.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.repository.GameSessionRepository;

@Service
public class GameSessionService {

    private final GameSessionRepository repo;

    public GameSessionService(GameSessionRepository repo) {
        this.repo = repo;
    }

    public PersistedGameSession createSession(String userId, String city) {
        repo.deleteByUserId(userId);  // first clear

        PersistedGameSession session = new PersistedGameSession(
            UUID.randomUUID().toString(),
            userId,
            city
        );

        return repo.save(session);
    }

    public Optional<PersistedGameSession> findByUserId(String userId) {
        return repo.findByUserId(userId);
    }

    public PersistedGameSession save(PersistedGameSession session) {
        session.setLastUpdated(Instant.now());
        return repo.save(session);
    }

    public void removeSession(String userId) {
        repo.deleteByUserId(userId);
    }

    public boolean hasSession(String userId) {
        return repo.findByUserId(userId).isPresent();
    }
}
