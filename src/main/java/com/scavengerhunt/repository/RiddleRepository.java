package com.scavengerhunt.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.Riddle;

public interface RiddleRepository extends MongoRepository<Riddle, String> {
    List<Riddle> findByLandmarkId(String landmarkId);
}
