package com.scavengerhunt.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scavengerhunt.model.AnswerTransactionRecord;

public interface AnswerTransactionRecordRepository extends MongoRepository<AnswerTransactionRecord, String>{
    List<AnswerTransactionRecord> findBySessionId(String sessionId);
    List<AnswerTransactionRecord> findByUserId(String userId);
    List<AnswerTransactionRecord> findByLandmarkId(String landmarkId);
}
