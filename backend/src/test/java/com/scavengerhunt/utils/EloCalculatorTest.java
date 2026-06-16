package com.scavengerhunt.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.User;
import com.scavengerhunt.repository.GameDataRepository;

public class EloCalculatorTest {

    @Mock private GameDataRepository mockGameDataRepo;

    private User testUser;
    private Landmark testLandmark;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User("testUser", "password");
        testUser.setUserId("user-001");
        testUser.setRating(0.6);
        testUser.setLastGameAt(LocalDateTime.now().minusDays(5));

        testLandmark = new Landmark(
        "lm-001",
        "Glucksman Gallery",
        "Cork",
        51.894741757894735,
        -8.490317963157894);
        testLandmark.setRating(0.5);
        testLandmark.setLastAnswered(LocalDateTime.now().minusDays(10));

        when(mockGameDataRepo.getUserById("user-001")).thenReturn(testUser);
        when(mockGameDataRepo.findLandmarkById("lm-001")).thenReturn(testLandmark);
    }

    @Test
    void correctAnswer_userRatingIncrease() {
        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        calc.updateRating("lm-001", 10, true);

        verify(mockGameDataRepo).updateUserRating(eq("user-001"), doubleThat(r -> r > 0.6));
        verify(mockGameDataRepo).updateLandmarkRating(eq("lm-001"), doubleThat(r -> r < 0.5));
    }

    @Test
    void wrongAnswer_userRatingDecrese() {
        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        calc.updateRating("lm-001", 10, false);

        verify(mockGameDataRepo).updateUserRating(eq("user-001"), doubleThat(r -> r < 0.6));
        verify(mockGameDataRepo).updateLandmarkRating(eq("lm-001"), doubleThat(r -> r > 0.6));
    }

    @Test
    void nullRating_usesDefaultAndDoesNotCrash() {
        testUser.setRating(null);
        testLandmark.setRating(null);

        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        assertDoesNotThrow(() -> calc.updateRating("lm-001", 10, true));
        verify(mockGameDataRepo).updateUserRating(eq("user-001"), doubleThat(r -> !Double.isNaN(r)));
    }

    @Test
    void equalRatings_doesNotThrowDivisionByZero() {
        testUser.setRating(0.5);
        testLandmark.setRating(0.5);

        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        assertDoesNotThrow(() -> calc.updateRating("lm-001", 10, true));
    }

    @Test
    void inactiveUser_highUncertainty_ratingStillUpdate() {
        testUser.setLastGameAt(LocalDateTime.now().minusDays(60));

        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        calc.updateRating("lm-001", 10, true);

        verify(mockGameDataRepo).updateUserRating(eq("user-001"), anyDouble());
        verify(mockGameDataRepo).updateLandmarkRating(eq("lm-001"), anyDouble());
    }

    @Test
    void newUser_nullLastGameAt_doesNotCrash() {
        testUser.setLastGameAt(null);

        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        assertDoesNotThrow(() -> calc.updateRating("lm-001", 10, true));
    }

    @Test
    void updateRating_refreshesUserFromDB() {
        // updateRating() calls getUserById() again inside to get fresh data
        EloCalculator calc = new EloCalculator("user-001", mockGameDataRepo, 30);
        calc.updateRating("lm-001", 10, true);

        verify(mockGameDataRepo, times(2)).getUserById("user-001"); // once in constructor, once in updateRating
    }
}
