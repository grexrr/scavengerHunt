package com.scavengerhunt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.BackgroundJob;

public interface BackgroundJobRepository extends MongoRepository<BackgroundJob, String>{
    Optional<BackgroundJob> findByIdempotencyKey(String key);
    List<BackgroundJob> findByStatus(BackgroundJob.Status status);
}
