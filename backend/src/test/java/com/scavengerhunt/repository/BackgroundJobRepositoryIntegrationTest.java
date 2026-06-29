package com.scavengerhunt.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.scavengerhunt.model.BackgroundJob;

public class BackgroundJobRepositoryIntegrationTest {

    @Autowired private BackgroundJobRepository repo;

    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void findByStatus_pendingreturnsOnlyPendingJobs() {



        repo.save(BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49));
        repo.save(BackgroundJob.fetchLandmarks("Dublin", 53.33, -6.24));
        BackgroundJob done = BackgroundJob.fetchLandmarks("Galway", 53.27, -9.05);
        done.setStatus(BackgroundJob.Status.DONE);
        repo.save(done);

        List<BackgroundJob> res = repo.findByStatus(BackgroundJob.Status.PENDING);

        assertEquals(2, res.size());
        assertTrue(res.stream().allMatch(j -> j.getStatus() == BackgroundJob.Status.PENDING));
    }

    @Test
    void findByStatus_failed_returnsOnlyFailedJobs() {


        repo.save(BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49));
        BackgroundJob failed = BackgroundJob.fetchLandmarks("Dublin", 53.33, -6.24);
        failed.setStatus(BackgroundJob.Status.FAILED);
        repo.save(failed);

        List<BackgroundJob> res = repo.findByStatus(BackgroundJob.Status.FAILED);

        assertEquals(1, res.size());
        assertEquals(BackgroundJob.Status.FAILED, res.get(0).getStatus());
    }

    @Test
    void findByStatus_noMatch_returnsEmptyList() {
        repo.save(BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49));
        List<BackgroundJob> result = repo.findByStatus(BackgroundJob.Status.DONE);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdempotencyKey_exists_returnsJob() {
        repo.save(BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49));

        Optional<BackgroundJob> res = repo.findByIdempotencyKey("fetch-landmark-cork");

        assertTrue(res.isPresent());
        assertEquals("Cork", res.get().getPayload().get("city"));
    }

    @Test
    void findByIdempotencyKey_notExists_returnsEmpty() {
        Optional<BackgroundJob> res = repo.findByIdempotencyKey("fetch-landmark-cork");
        assertTrue(res.isEmpty());
    }

    @Test
    void findByIdempotencyKey_multipleSavedJobs_returnsCorrectOne() {
        repo.save(BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49));
        repo.save(BackgroundJob.fetchLandmarks("Dublin", 53.33, -6.24));

        Optional<BackgroundJob> res = repo.findByIdempotencyKey("fetch-landmarks-dublin");

        assertTrue(res.isPresent());
        assertEquals("Dublin", res.get().getPayload().get("city"));
    }
}
