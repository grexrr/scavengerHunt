package com.scavengerhunt.game;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.model.AnswerTransactionRecord;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.model.Player;
import com.scavengerhunt.repository.AnswerTransactionRecordRepository;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.EloCalculator;
import com.scavengerhunt.utils.GeoUtils;

public class GameLogicManager {
    private static final Logger log = LoggerFactory.getLogger(GameLogicManager.class);

    private String userId;
    private Player player;

    private GameDataRepository gameDataRepo;
    private PersistedGameSession session;

    private PlayerStateManager playerStateManager;
    private LandmarkManager landmarkManager;
    private PuzzleManager puzzleManager;

    private EloCalculator eloCalculator;
    private AnswerTransactionRecordRepository answerTransactionRecordRepo;

    private Landmark currentTarget;
    private Map<String, Integer> attemptsByLandmarkId;
    private Map<String, Boolean> solvedLandmarks = new HashMap<>(); // only for frontend rendering

    private int maxWrongAnswer = 3;
    private int maxRiddleDurationMinutes = 30;

    public GameLogicManager(
        PersistedGameSession session,
        GameDataRepository gameDataRepo,
        LandmarkProcessorClient landmarkProcessorClient,
        PuzzleAgentClient puzzleAgentClient,
        AnswerTransactionRecordRepository answerTransactionRecordRepo,
        int maxRiddleDurationMinutes
    ) {
        this.session = session;
        this.gameDataRepo = gameDataRepo;

        this.userId = session.getUserId();
        this.player = new Player(
            session.getPlayerLat(),
            session.getPlayerLng(),
            session.getPlayerAngle(),
            session.getCity()
        );

        this.landmarkManager = new LandmarkManager(gameDataRepo, landmarkProcessorClient, session.getCity());

        this.playerStateManager = new PlayerStateManager(this.player, this.landmarkManager, this.gameDataRepo);

        this.puzzleManager = new PuzzleManager(gameDataRepo, puzzleAgentClient);

        this.attemptsByLandmarkId = session.getAttemptsByLandmarkId();
        if (session.getCurrentTargetId() != null){
            this.currentTarget = gameDataRepo.findLandmarkById(session.getCurrentTargetId());
        }

        this.eloCalculator = new EloCalculator(this.userId, this.gameDataRepo, this.maxRiddleDurationMinutes);

        this.answerTransactionRecordRepo = answerTransactionRecordRepo;
    }

    // Package-private: only accessible from com.scavengerhunt.game — where tests live
    GameLogicManager(
        PersistedGameSession session,
        GameDataRepository gameDataRepo,
        PlayerStateManager playerStateManager,
        PuzzleAgentClient puzzleAgentClient,
        AnswerTransactionRecordRepository answerTransactionRecordRepo,
        int maxRiddleDurationMinutes
    ) {
        this.session = session;
        this.gameDataRepo = gameDataRepo;
        this.userId = session.getUserId();
        this.player = new Player(
            session.getPlayerLat(),
            session.getPlayerLng(),
            session.getPlayerAngle(),
            session.getCity()
        );
        this.landmarkManager = null;
        this.playerStateManager = playerStateManager;
        this.puzzleManager = new PuzzleManager(gameDataRepo, puzzleAgentClient);
        this.attemptsByLandmarkId = session.getAttemptsByLandmarkId();
        if (session.getCurrentTargetId() != null) {
            this.currentTarget = gameDataRepo.findLandmarkById(session.getCurrentTargetId());
        }
        this.eloCalculator = new EloCalculator(this.userId, this.gameDataRepo, maxRiddleDurationMinutes);
        this.answerTransactionRecordRepo = answerTransactionRecordRepo;
        this.maxRiddleDurationMinutes = maxRiddleDurationMinutes;
    }

    public void updatePlayerPosition(double lat, double lng, double angle){
        this.playerStateManager.updatePlayerPosition(lat, lng, angle);
        this.session.updatePlayerPosition(lat, lng, angle);
        log.debug("Updated position: {}, {} @ {}", lat, lng, angle);
    }

    public void startNewRound(double radiusMeters) {

        double lat = this.playerStateManager.getPlayer().getLatitude();
        double lng = this.playerStateManager.getPlayer().getLongitude();

        // clear currentTarget!
        this.playerStateManager.resetGame();
        this.currentTarget = null;

        // init candidate map
        this.landmarkManager.getRoundLandmarksIdWithinRadius(lat, lng, radiusMeters);
        List<Landmark> candidateLandmarks = this.landmarkManager.getAllRouLandmark();

        this.puzzleManager.initialize(this.userId, candidateLandmarks, null, null);

        // Reset PuzzleAgent session to start fresh
        this.puzzleManager.resetPuzzleSession();

        log.debug("Candidate landmarks found: {}", candidateLandmarks.size());

        if (candidateLandmarks.isEmpty()) {
            log.warn("No landmarks found within radius {}m for user {}. Cannot start game.", radiusMeters, this.userId);
            this.playerStateManager.setGameFinished();
            syncToSession();
            return;
        }

        // map the current answer counter (starts from 3, decreases to 0)
        this.attemptsByLandmarkId = candidateLandmarks.stream()
            .collect(Collectors.toMap(landmark -> landmark.getId(), landmark -> maxWrongAnswer));

        // select the first target & set round start time
        selectNextTarget();
        syncToSession();
        log.info("New game round started for user {} with {} targets", this.userId, this.attemptsByLandmarkId.size());
    }

    public boolean submitCurrentAnswer(long riddleSeconds) {

        if (this.currentTarget == null) {
            log.debug("submitCurrentAnswer called with no current target for user {}", this.userId);
            return false;
        };


        // check finishing time
        if (riddleSeconds >= maxRiddleDurationMinutes * 60L) {
            log.debug("Time limit exceeded ({}s) -> auto-fail for target {}", riddleSeconds, this.currentTarget.getId());
            return singleTransaction(riddleSeconds, false);
        }

        //check if detected landmark == currentTarget
        this.playerStateManager.updateDetectedLandmark(); // Force update detected landmark before checking
        Landmark detectedLandmark = this.playerStateManager.getDetectedLandmark();
        log.debug("Target={} detected={}", this.currentTarget.getId(), detectedLandmark != null ? detectedLandmark.getId() : "null");

        // for the sake of robustness!
        // if not detected landmark, front end does not allow submission button anyway
        if (detectedLandmark == null) {
            //if no landmark detected, current wrong count + 1
            return singleTransaction(riddleSeconds,false);
        }

        //if detected landmark is not within attemptsByLandmarkId
        if (detectedLandmark != null && !this.attemptsByLandmarkId.containsKey(detectedLandmark.getId())) {
            return singleTransaction(riddleSeconds,false);
        } else {
            //if it's within attemptsByLandmarkId
            if (answerCorrect(detectedLandmark)){
                //if correct
                return singleTransaction(riddleSeconds,true);
            } else {
                //if incorrect
                return singleTransaction(riddleSeconds,false);
            }
        }
    }

    // ==================== Helper Functions ====================

    private void syncToSession() {
        session.setAttemptsByLandmarkId(this.attemptsByLandmarkId);
        session.setCurrentTargetId(this.currentTarget != null ? this.currentTarget.getId() : null);
        session.setFinished(isGameFinished());
        session.setLastUpdated(Instant.now());
    }

    private Landmark selectNextTarget() {
        // select Nearest for MVP
        if (!this.playerStateManager.isGameFinished()) {
            // Check if target pool is empty
            if (getUnsolvedLandmarks().isEmpty()) {
                log.info("Target pool empty, game finished for user {}", this.userId);
                this.playerStateManager.setGameFinished();
                this.currentTarget = null;
                this.puzzleManager.storeUserGameRoundStatistics();
                return null;
            }

            Landmark last = this.currentTarget;
            if (last == null){
                double playerLat = playerStateManager.getPlayer().getLatitude();
                double playerLng = playerStateManager.getPlayer().getLongitude();
                this.currentTarget = selectNearestTo(playerLat, playerLng);
            } else {
                this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
            }

            log.debug("Selected target: {}", this.currentTarget != null ? this.currentTarget.getId() : "null");

            //Generate Riddle only if target is found
            if (this.currentTarget != null) {

                String riddle = this.puzzleManager.getRiddleForLandmark(this.currentTarget.getId());
                this.currentTarget.setRiddle(riddle);

                // Force update detected landmark when target changes
                this.playerStateManager.updateDetectedLandmark();
            }

        } else {
            // puzzleManager.pausePuzzleTimer(currentTarget);
            this.currentTarget = null;
        }

        return this.currentTarget;
    }


    private boolean singleTransaction(long riddleSeconds, Boolean isCorrect){
        // Only proceed if game has not finished (i.e., game is active)
        if (this.playerStateManager.isGameFinished()) {
            log.debug("Ignoring transaction, game already finished for user {}", this.userId);
            return false;
        }

        if (isCorrect == false){
            this.attemptsByLandmarkId.put(this.currentTarget.getId(), this.attemptsByLandmarkId.get(this.currentTarget.getId()) - 1);

            // Check if current target has reached 0 attempts
            if (this.attemptsByLandmarkId.get(this.currentTarget.getId()) <= 0) {
                log.debug("Target {} exhausted attempts, removing from pool", this.currentTarget.getId());

                //pool removal and update rating (User & currentTarget)
                String lmid = this.currentTarget.getId();
                this.attemptsByLandmarkId.remove(lmid);
                eloCalculator.updateRating(lmid, riddleSeconds, isCorrect);

                AnswerTransactionRecord record = AnswerTransactionRecord.forAttempt(
                    this.session.getSessionId(),
                    this.session.getUserId(),
                    lmid, isCorrect,
                    maxWrongAnswer - this.attemptsByLandmarkId.getOrDefault(lmid, 0),
                    riddleSeconds
                );
                answerTransactionRecordRepo.save(record);

                // add to solved landmark as wrong
                this.solvedLandmarks.put(this.currentTarget.getId(), isCorrect);

                // Check if this was the last target
                if (checkAndHandleGameEnd()) {
                    return isCorrect; // Game finished
                } else {
                    return isCorrect; // Continue with next target
                }
            }
            syncToSession();
            return isCorrect;
        } else {
            log.debug("Answer correct for target {}", this.currentTarget.getId());

            //pool removal and update rating (User & currentTarget)
            String lmid = this.currentTarget.getId();
            this.attemptsByLandmarkId.remove(lmid);
            eloCalculator.updateRating(lmid, riddleSeconds, isCorrect);
            AnswerTransactionRecord record = AnswerTransactionRecord.forAttempt(
                    this.session.getSessionId(),
                    this.session.getUserId(),
                    lmid, isCorrect,
                    maxWrongAnswer - this.attemptsByLandmarkId.getOrDefault(lmid, 0),
                    riddleSeconds
                );
                answerTransactionRecordRepo.save(record);

            this.solvedLandmarks.put(this.currentTarget.getId(), isCorrect);

            // Check if this was the last target
            if (checkAndHandleGameEnd()) {
                return isCorrect; // Game completed successfully
            } else {
                return isCorrect; // Continue with next target
            }
        }
    }

    private boolean checkAndHandleGameEnd() {
        // Only handle game end if game is active (not already finished)
        if (this.attemptsByLandmarkId.isEmpty()) {
            this.playerStateManager.setGameFinished();
            this.currentTarget = null;
            this.puzzleManager.storeUserGameRoundStatistics();
            this.puzzleManager.resetPuzzleSession();
            syncToSession();
            return true; // Game finished
        } else {
            selectNextTarget();
            syncToSession();
            return false; // Continue with next target
        }
    }

    private Landmark selectNearestTo(double refLat, double refLng) {
        return getUnsolvedLandmarks().entrySet().stream()
            .map(entry -> gameDataRepo.findLandmarkById(entry.getKey()))
            .filter(landmark -> landmark != null)
            .min((l1, l2) -> {
                double d1 = GeoUtils.distanceInMeters(refLat, refLng, l1.getLatitude(), l1.getLongitude());
                double d2 = GeoUtils.distanceInMeters(refLat, refLng, l2.getLatitude(), l2.getLongitude());
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    public boolean answerCorrect(Landmark detectedLandmark) {
        if (playerStateManager.getDetectedLandmark() == null || currentTarget == null) return false;

        boolean isCorrect = detectedLandmark.getId().equals(this.currentTarget.getId());
        log.debug("Comparing detected={} target={} -> {}", detectedLandmark.getId(), this.currentTarget.getId(), isCorrect);

        return isCorrect;
    }

    public boolean isGameFinished() {
        if (this.playerStateManager.isGameFinished()) {
            return true;
        }
        return this.attemptsByLandmarkId != null && this.attemptsByLandmarkId.isEmpty();
    }

    // ==================== Getters & Setters ====================

    public Map<String, Object> getCurrentTarget() {
        // If game is finished, don't try to select new targets
        if (this.playerStateManager.isGameFinished()) {
            return null;
        }

        if (this.attemptsByLandmarkId == null || this.attemptsByLandmarkId.isEmpty()) {
            return null;
        }

        // If current target is null, try to select next target
        if (this.currentTarget == null) {
            this.currentTarget = selectNextTarget();
        }

        if (this.currentTarget == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", this.currentTarget.getId());
        result.put("name", this.currentTarget.getName());
        result.put("attemptsLeft", this.attemptsByLandmarkId.get(this.currentTarget.getId()));
        result.put("riddle", this.currentTarget.getRiddle());
        return result;
    }

    public PuzzleManager getPuzzleManager() {
        return puzzleManager;
    }

    public void setPuzzleManager(PuzzleManager puzzleManager) {
        this.puzzleManager = puzzleManager;
    }

    public String getUserId() {
        return userId;
    }

    public PlayerStateManager getPlayerState() {
        return this.playerStateManager;
    }

    public Map<String, Integer> getUnsolvedLandmarks() {
        return this.attemptsByLandmarkId;
    }

    public void setAttemptsByLandmarkId(Map<String, Integer> attempts) {
        this.attemptsByLandmarkId = attempts;
    }

    public void setCurrentTarget(Landmark target) {
        this.currentTarget = target;
    }
}


