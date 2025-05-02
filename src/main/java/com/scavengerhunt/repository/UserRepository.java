package com.scavengerhunt.repository;


import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.User;

public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(String userId);
} 