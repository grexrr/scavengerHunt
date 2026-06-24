package com.scavengerhunt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.model.BackgroundJob;
import com.scavengerhunt.repository.BackgroundJobRepository;

public class JobCoordinatorTest {

    @Mock private BackgroundJobRepository jobRepo;
    @Mock private LandmarkIngestionService landmarkIngestionService;

    private JobCoordinator coordinator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        coordinator = new JobCoordinator(jobRepo, landmarkIngestionService);
        when(jobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void enqueueFetchLandmarks_newCity_savesNewJob() {
        when(jobRepo.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        BackgroundJob saved = BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49);
        when(jobRepo.save(any())).thenReturn(saved);

        BackgroundJob job = coordinator.enqueueFetchLandmarks("Cork", 51.89, -8.49);

        verify(jobRepo).save(any());
        assertEquals(BackgroundJob.Status.PENDING, job.getStatus());
    }

    @Test
    void enqueueFetchLandmarks_sameCity_returnsExistingJob() {
        BackgroundJob existing = BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49);

        when(jobRepo.findByIdempotencyKey("fetch-landmarks-cork")).thenReturn(Optional.of(existing));

        verify(jobRepo, never()).save(any());
    }

    @Test
    void enqueueFetchLandmarks_cityWithSpaces_normalisedKey() {
        when(jobRepo.findByIdempotencyKey("fetch-landmarks-new-york")).thenReturn(Optional.empty());

        when(jobRepo.save(any())).thenReturn(
            BackgroundJob.fetchLandmarks("New York", 40.71, -74.00)
        );

        coordinator.enqueueFetchLandmarks("New York", 40.71, -74.00);

        verify(jobRepo).findByIdempotencyKey("fetch-landmarks-new-york");
    }

    @Test
    void processPendingJobs_jobAtMaxAttempts_setsStatusFailed() {
        BackgroundJob job = BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49);
        job.setAttemptCount(3);

        when(jobRepo.findByStatus(BackgroundJob.Status.PENDING)).thenReturn(List.of(job));

        coordinator.processPendingJobs();

        assertEquals(BackgroundJob.Status.FAILED, job.getStatus());
        verify(jobRepo).save(job);
        verifyNoInteractions(landmarkIngestionService);
    }

    @Test
    void processPendingJobs_jobBelowMaxAttempts_callsProcessJob() {
        BackgroundJob job = BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49);
        job.setAttemptCount(0);

        when(jobRepo.findByStatus(BackgroundJob.Status.PENDING)).thenReturn(List.of(job));

        coordinator.processPendingJobs();

        assertEquals(BackgroundJob.Status.DONE, job.getStatus());
        assertNotNull(job.getCompletedAt());
        assertEquals(1, job.getAttemptCount());
        // save() is called twice: once at start (IN_PROGRESS), once at end (DONE)
        verify(landmarkIngestionService).fetchForLocation(51.89, -8.49);
        verify(jobRepo, times(2)).save(job);
    }

    @Test
    void processJob_serviceThrows_setStatusPendingForEntry() {
        BackgroundJob job = BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49);
        job.setAttemptCount(0);

        when(jobRepo.findByStatus(BackgroundJob.Status.PENDING)).thenReturn(List.of(job));
        doThrow(new RuntimeException("network error"))
            .when(landmarkIngestionService).fetchForLocation(anyDouble(), anyDouble());

        coordinator.processPendingJobs();

        assertEquals(BackgroundJob.Status.PENDING, job.getStatus());
        assertEquals("network error", job.getLastError());
        assertEquals(1, job.getAttemptCount());
    }

    @Test
    void processJob_serviceThrows_doesNotSetCompletedAt() {
        BackgroundJob job = BackgroundJob.fetchLandmarks("Cork", 51.89, -8.49);
        job.setAttemptCount(0);

        when(jobRepo.findByStatus(BackgroundJob.Status.PENDING)).thenReturn(List.of(job));
        doThrow(new RuntimeException("timeout"))
            .when(landmarkIngestionService).fetchForLocation(anyDouble(), anyDouble());

        coordinator.processPendingJobs();

        assertNull(job.getCompletedAt(), "completedAt should not be set on failure");
    }
}
