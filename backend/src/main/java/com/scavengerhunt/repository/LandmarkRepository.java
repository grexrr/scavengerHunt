package com.scavengerhunt.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.scavengerhunt.model.Landmark;

public interface LandmarkRepository extends MongoRepository<Landmark, String> {
    @Query(value = "{}", fields = "{'_id': 1}")
    List<String> findAllId();
    
    @Query(value = "{'city': ?0}")
    List<Landmark> findByCity(String city);

    @Query(value = "{'_id': ?0}", fields = "{'name': 1}")
    String findNameById(String id);
    
    @Query(value = "{'_id': ?0}", fields = "{'_id': 1, 'geometry.coordinates': 1}")
    Map<String, List<List<Double>>> getCoordinatesById(String id);
}
