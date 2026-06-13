package com.scavengerhunt.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class AnswerTransactionRecordTest {

    @Test
    void forAttempt_setsAllFieldsCorrectly() {
        LocalDateTime before = LocalDateTime.now();

        AnswerTransactionRecord record = AnswerTransactionRecord.forAttempt(
            "session-001",
            "user-alex",
            "id-glucksman",
            true,
            2,
            120L
        );

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(record.getId());
        assertEquals("session-001", record.getSessionId());
        assertEquals("user-alex", record.getUserId());
        assertEquals("id-glucksman", record.getLandmarkId());
        assertTrue(record.isCorrect());
        assertEquals(2, record.getAttemptCount());
        assertEquals(120L, record.getRiddleDurationSeconds());
        assertNotNull(record.getCreatedAt());
        assertFalse(record.getCreatedAt().isBefore(before));
        assertFalse(record.getCreatedAt().isAfter(after));
    }

    @Test
    void forAttempt_generatesUniqueIds() {
        AnswerTransactionRecord r1 = AnswerTransactionRecord.forAttempt("s1", "u1", "l1", true, 1, 10L);
        AnswerTransactionRecord r2 = AnswerTransactionRecord.forAttempt("s1", "u1", "l1", true, 1, 10L);

        assertNotEquals(r1.getId(), r2.getId());
    }

    @Test
    void forAttempt_incorrectAnswer_isCorrectFalse() {
        AnswerTransactionRecord record = AnswerTransactionRecord.forAttempt(
            "session-001", "user-alex", "id-glucksman", false, 3, 600L
        );
        assertFalse(record.isCorrect());
    }
}
