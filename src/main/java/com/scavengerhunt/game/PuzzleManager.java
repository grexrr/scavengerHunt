package com.scavengerhunt.game;

import java.util.List;

import org.springframework.stereotype.Component;

import com.scavengerhunt.model.Riddle;
import com.scavengerhunt.repository.RiddleRepository;

@Component
public class PuzzleManager {
    private RiddleRepository riddleRepo;

    public PuzzleManager(RiddleRepository riddleRepo) {
        this.riddleRepo = riddleRepo;
    }

    public String getRiddleForLandmark(String landmarkId) {
        List<Riddle> options = riddleRepo.findByLandmarkId(landmarkId);
        for (Riddle r : options) {
            if ("mysterious".equalsIgnoreCase(r.getStyle())) {
                return r.getContent();
            }
        }
        return "(No riddle available)";
    }
}


