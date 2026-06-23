package com.scavengerhunt.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.scavengerhunt.model.BackgroundJob;
import com.scavengerhunt.repository.BackgroundJobRepository;

@Service
public class JobCoordinator {

    private static final Logger log = LoggerFactory.getLogger(JobCoordinator.class);
    private static final int MAX_ATTEMPTS = 3;

    private final BackgroundJobRepository jobRepo;
    private final LandmarkIngestionService landmarkIngestionService;

    public JobCoordinator(BackgroundJobRepository jobRepo, LandmarkIngestionService landmarkIngestionService){
        this.jobRepo = jobRepo;
        this.landmarkIngestionService = landmarkIngestionService;
    }

    public BackgroundJob enqueueFetchLandmarks(String city, double lat, double lng) {
        String key = "fetch-landmarks-" + city.toLowerCase().replaceAll("\\s+", "-");
        return jobRepo.findByIdempotencyKey(key)
            .orElseGet(() -> jobRepo.save(BackgroundJob.fetchLandmarks(city, lat, lng)));
    }

    @Scheduled(fixedDelay = 5000)
    public void processPendingJobs() {
        List<BackgroundJob> jobs = jobRepo.findByStatus(BackgroundJob.Status.PENDING);
        for(BackgroundJob job: jobs) {
            if(job.getAttemptCount() >= MAX_ATTEMPTS) {
                job.setStatus(BackgroundJob.Status.FAILED);
                jobRepo.save(job);
                continue;
            }
            processJob(job);
        }
    }

    private void processJob(BackgroundJob job) {
        job.setStatus(BackgroundJob.Status.IN_PROGRESS);
        job.setStartedAt(Instant.now());
        job.setAttemptCount(job.getAttemptCount() + 1);
        jobRepo.save(job);

        try {
            if(job.getType() == "FETCH_LANDMARKS"){
                double lat = (double) job.getPayload().get("latitude");
                double lng = (double) job.getPayload().get("longitude");
                //landmarks ingestion service fetch for location
            }
            job.setStatus(BackgroundJob.Status.DONE);
            job.setCompletedAt(Instant.now());
            log.info("Job {} ({}) completed", job.getJobId(), job.getType());
        } catch (Exception e) {
            job.setStatus(BackgroundJob.Status.PENDING); // attempts retry
            job.setLastError(e.getMessage());
            log.warn("Job {} failed: {}", job.getJobId(), e.getMessage());
        }
        jobRepo.save(job);
    }
}
