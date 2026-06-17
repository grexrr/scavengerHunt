package com.scavengerhunt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.scavengerhunt.model.AnswerTransactionRecord;
import com.scavengerhunt.repository.AnswerTransactionRecordRepository;

@SpringBootTest
public class AnswerTransactionRecordRepoIntegrationTest {

    @Autowired private AnswerTransactionRecordRepository repo;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void findBySessionId_returnOnlyMatchingSession(){
        repo.save(AnswerTransactionRecord.forAttempt("session-A","user-1", "lm-1", true, 1, 60L));
        repo.save(AnswerTransactionRecord.forAttempt("session-A","user-1", "lm-2", false, 3, 120L));
        repo.save(AnswerTransactionRecord.forAttempt("session-B","user-2", "lm-1", true, 1, 30L));

        List<AnswerTransactionRecord> res = repo.findBySessionId("session-A");

        assertEquals(2, res.size());
        assertTrue(res.stream().allMatch(id -> id.getSessionId().equals("session-A")));
    }

    @Test
    void findByUserId_returnsAllUserRecords(){
        repo.save(AnswerTransactionRecord.forAttempt("s1", "user-1", "lm-1", true, 1, 60L));
        repo.save(AnswerTransactionRecord.forAttempt("s2", "user-1", "lm-2", false, 2, 90L));
        repo.save(AnswerTransactionRecord.forAttempt("s3", "user-2", "lm-1", true, 1, 45L));

        List<AnswerTransactionRecord> res = repo.findByUserId("user-1");

        assertEquals(2, res.size());
    }

    @Test
    void findByLandmarkId_returnsAllAttemptsForLandmark(){
        repo.save(AnswerTransactionRecord.forAttempt("s1", "user-1", "lm-glucksman", true, 1, 60L));
        repo.save(AnswerTransactionRecord.forAttempt("s2", "user-2", "lm-glucksman", false, 3, 200L));
        repo.save(AnswerTransactionRecord.forAttempt("s3", "user-1", "lm-quad", true, 1, 30L));

        List<AnswerTransactionRecord> res = repo.findByLandmarkId("lm-glucksman");

        assertEquals(2, res.size());
    }

    @Test
    void findBySessionId_noMatch_returnsEmptyList() {
        List<AnswerTransactionRecord> result = repo.findBySessionId("nonexistent-session");
        assertTrue(result.isEmpty());
    }
}
