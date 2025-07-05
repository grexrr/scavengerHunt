package com.scavengerhunt.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.Landmark;

public interface LandmarkRepository extends MongoRepository<Landmark, String> {
    List<String> findAllId();
    List<String > findIdByCity(String city);

    String findNameById(String id);
    
}
