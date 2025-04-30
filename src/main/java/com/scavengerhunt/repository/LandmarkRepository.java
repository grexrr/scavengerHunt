package com.scavengerhunt.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.Landmark;

public interface LandmarkRepository extends MongoRepository<Landmark, String> {
    
}
