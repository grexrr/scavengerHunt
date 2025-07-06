package com.scavengerhunt.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.LandmarkRepository;
import com.scavengerhunt.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final LandmarkRepository landmarkRepository;
    private final UserRepository userRepository;
    
    public AdminController(LandmarkRepository landmarkRepository, UserRepository userRepository){
        this.landmarkRepository = landmarkRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/insert-landmarks")
    public ResponseEntity<String> insertLandmarks() {
        List<Landmark> landmarks = List.of(
            new Landmark("Glucksman Gallery", "Cork", 51.8947384, -8.4903073),
            new Landmark("Honan Chapel", "Cork", 51.8935836, -8.49002395),
            new Landmark("Boole Library", "Cork", 51.892795899999996,-8.491407089727364)
        );

        landmarkRepository.saveAll(landmarks);
        return ResponseEntity.ok("[Admin] Landmarks inserted.");
    }

    @PostMapping("/insert-users")
    public ResponseEntity<String> insertUsers() {
        List<User> users = List.of(
            new User("Bob", "12345", true)
        );
        userRepository.saveAll(users);
        return ResponseEntity.ok("[Admin] Users inserted.");
    }

    @DeleteMapping("/clear-landmarks")
    public ResponseEntity<String> clearLandmarks() {
        landmarkRepository.deleteAll();
        return ResponseEntity.ok("[Admin] All landmarks cleared.");
    }

    @DeleteMapping("/clear-users")
    public ResponseEntity<String> clearUsers() {
        userRepository.deleteAll();
        return ResponseEntity.ok("[Admin] All users cleared.");
    }
}
