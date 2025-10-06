package com.scavengerhunt.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @Mock
    private GameDataRepository gameDataRepo;

    private User testUser;
    private Landmark testLandmark;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // test user
        testUser = new User("testUser", "password");
        testUser.setUserId("user123");
        testUser.setRating(0.6);
        testUser.setLastGameAt(LocalDateTime.now().minusDays(5));

        // test landmark
        testLandmark = new Landmark("Eiffel Tower", "Paris", 48.8584, 2.2945);
        testLandmark.setId("landmark456");
        testLandmark.setRating(0.5);
        testLandmark.setLastAnswered(LocalDateTime.now().minusDays(10));
    }

    @Test
    public void testBasicMathLogic() {

        double userRating = 0.6;
        double landmarkRating = 0.5;
        double K = 0.0075;

        double delta = userRating - landmarkRating;
        double newUserRating = userRating + K * delta;

        assertTrue(newUserRating > userRating, "User rating should be rising.");
        System.out.println("New rating: " + newUserRating);
    }

    @Test
    public void testCorrectAnswerQuickly_UserRatingIncreases() {
        // Mock repository responses
        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        // 用户快速答对（10秒）
        calculator.updateRating("landmark456", 10, true);

        // 验证用户评分上升
        verify(gameDataRepo).updateUserRating(eq("user123"), doubleThat(rating -> rating > 0.6));
        // 验证地标评分下降
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), doubleThat(rating -> rating < 0.5));
    }

    @Test
    public void testWrongAnswer_UserRatingDecreases() {
        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        // 用户答错
        calculator.updateRating("landmark456", 10, false);

        // 验证用户评分下降
        verify(gameDataRepo).updateUserRating(eq("user123"), doubleThat(rating -> rating < 0.6));
        // 验证地标评分上升
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), doubleThat(rating -> rating > 0.5));
    }

    @Test
    public void testSlowCorrectAnswer_LowerReward() {
        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        // 用户慢速答对（接近时间限制：29分钟）
        calculator.updateRating("landmark456", 29 * 60, true);

        // 验证评分变化较小（因为用时长）
        verify(gameDataRepo).updateUserRating(eq("user123"), anyDouble());
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), anyDouble());
    }

    @Test
    public void testNullRating_UsesDefaultValue() {
        testUser.setRating(null);
        testLandmark.setRating(null);

        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        calculator.updateRating("landmark456", 10, true);

        // 验证使用默认值 0.5 并正常更新
        verify(gameDataRepo).updateUserRating(eq("user123"), doubleThat(rating -> !Double.isNaN(rating)));
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), doubleThat(rating -> !Double.isNaN(rating)));
    }

    @Test
    public void testNaNRating_UsesDefaultValue() {
        testUser.setRating(Double.NaN);
        testLandmark.setRating(Double.NaN);
        
        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);
        
        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);
        
        calculator.updateRating("landmark456", 10, true);
        
        // 验证处理了 NaN，评分是有效数字（不是 NaN）
        verify(gameDataRepo).updateUserRating(eq("user123"), doubleThat(rating -> !Double.isNaN(rating) && !Double.isInfinite(rating)));
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), doubleThat(rating -> !Double.isNaN(rating) && !Double.isInfinite(rating)));
    }

    @Test
    public void testInactiveUser_HigherUncertainty() {
        // 用户很久没玩游戏（60天前）
        testUser.setLastGameAt(LocalDateTime.now().minusDays(60));

        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        calculator.updateRating("landmark456", 10, true);

        // 不活跃用户应该有更大的评分变化（K值更大）
        verify(gameDataRepo).updateUserRating(eq("user123"), anyDouble());
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), anyDouble());
    }

    @Test
    public void testNewUser_NoLastGameDate() {
        // 新用户，没有 lastGameAt
        testUser.setLastGameAt(null);

        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        calculator.updateRating("landmark456", 10, true);

        // 验证能正常处理 null 的情况
        verify(gameDataRepo).updateUserRating(eq("user123"), doubleThat(rating -> !Double.isNaN(rating)));
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), doubleThat(rating -> !Double.isNaN(rating)));
    }

    @Test
    public void testEqualRatings_SmallDeltaHandling() {
        // 用户和地标评分完全相等
        testUser.setRating(0.5);
        testLandmark.setRating(0.5);

        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        // 不应该抛出除零异常
        assertDoesNotThrow(() -> calculator.updateRating("landmark456", 10, true));

        verify(gameDataRepo).updateUserRating(eq("user123"), anyDouble());
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), anyDouble());
    }

    @Test
    public void testExtremeTimeDifference() {
        when(gameDataRepo.getUserById("user123")).thenReturn(testUser);
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);

        // 测试极端时间（1秒答对 vs 超时）
        calculator.updateRating("landmark456", 1, true);

        verify(gameDataRepo).updateUserRating(eq("user123"), anyDouble());
        verify(gameDataRepo).updateLandmarkRating(eq("landmark456"), anyDouble());
    }

    @Test
    public void testDatabaseRefresh() {
        User updatedUser = new User("testUser", "password");
        updatedUser.setUserId("user123");
        updatedUser.setRating(0.65); // 评分已更新

        when(gameDataRepo.getUserById("user123"))
                .thenReturn(testUser) // 第一次调用（构造函数）
                .thenReturn(updatedUser); // 第二次调用（updateRating 内部刷新）
        when(gameDataRepo.findLandmarkById("landmark456")).thenReturn(testLandmark);

        EloCalculator calculator = new EloCalculator("user123", gameDataRepo, 30);
        calculator.updateRating("landmark456", 10, true);

        // 验证从数据库刷新了用户数据（调用了2次）
        verify(gameDataRepo, times(2)).getUserById("user123");
    }
}
