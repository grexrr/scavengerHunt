package com.scavengerhunt.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.PersistedGameSession;;;

public interface GameSessionRepository extends MongoRepository<PersistedGameSession, String>{

    Optional<PersistedGameSession> findByUserId(String userId);

    void deleteByUserId(String userId);
}
