package com.scavengerhunt.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.EloCalculator;
import com.scavengerhunt.utils.GeoUtils;

public class GameSession {
    private String userId;

    private GameDataRepository gameDataRepository;

    private PlayerStateManager playerState;
    private LandmarkManager landmarkManager;
    private PuzzleManager puzzleManager;
    public PuzzleManager getPuzzleManager() {
        return puzzleManager;
    }

    public void setPuzzleManager(PuzzleManager puzzleManager) {
        this.puzzleManager = puzzleManager;
    }

    private EloCalculator eloCalculator;

    private Landmark currentTarget;
    private Map<String, Integer> attemptsByLandmarkId; 
    private Map<String, Boolean> solvedLandmarks = new HashMap<>(); // only for frontend rendering

    private int maxWrongAnswer = 3;
    private int maxRiddleDurationMinutes = 30;

    public GameSession(
        String userId, 
        GameDataRepository gameDataRepository, 
        PlayerStateManager playerState, 
        LandmarkManager landmarkManager, 
        PuzzleManager puzzleManager,
        int maxRiddleDurationMinutes
        ){
        this.userId = userId;
        this.gameDataRepository = gameDataRepository;
        this.playerState = playerState;
        this.landmarkManager = landmarkManager;
        this.puzzleManager = puzzleManager;
        this.eloCalculator = new EloCalculator(userId, gameDataRepository, maxRiddleDurationMinutes);
    }

    // ==================== Core Functions ====================

    public void updatePlayerPosition(double lat, double lng, double angle){
        this.playerState.updatePlayerPosition(lat, lng, angle);
        System.out.println("[Session] Updated position: " + lat + ", " + lng + " @ " + angle);
    }

    public void startNewRound(double radiusMeters) {
        double lat = this.playerState.getPlayer().getLatitude();
        double lng = this.playerState.getPlayer().getLongitude();
        
        // clear currentTarget!
        this.playerState.resetGame();
        this.currentTarget = null;
        
        // init candidate map
        this.landmarkManager.getRoundLandmarksIdWithinRadius(lat, lng, radiusMeters);
        List<Landmark> candidateLandmarks = this.landmarkManager.getAllRouLandmark();

        System.out.println("[Debug] Candidate landmarks found: " + candidateLandmarks.size());
        for (Landmark lm : candidateLandmarks) {
            System.out.println("[Debug] - " + lm.getName() + " (ID: " + lm.getId() + ")");
        }

        if (candidateLandmarks.isEmpty()) {
            System.out.println("[Error] No landmarks found within radius " + radiusMeters + " meters. Cannot start game.");
            this.playerState.setGameFinished();
            return;
        }

        // map the current answer counter (starts from 3, decreases to 0)
        this.attemptsByLandmarkId = candidateLandmarks.stream()
            .collect(Collectors.toMap(landmark -> landmark.getId(), landmark -> maxWrongAnswer));

        System.out.println("[Debug] Target pool created with " + this.attemptsByLandmarkId.size() + " landmarks");

        // select the first target & set round start time
        selectNextTarget();
        System.out.println("[Session] New game round started.");
    }

    public boolean submitCurrentAnswer(long riddleSeconds) {

        if (this.currentTarget == null) {
            System.out.println("[Session] No current Target.");
            return false;
        };

        System.out.println("[Debug] Current target: " + this.currentTarget.getName() + " (ID: " + this.currentTarget.getId() + ")");
        System.out.println("[Debug] Target pool size: " + this.attemptsByLandmarkId.size());
        System.out.println("[Debug] Target pool contents: " + this.attemptsByLandmarkId.keySet());
        
        // check finishing time
        if (riddleSeconds > maxRiddleDurationMinutes * 60L) {
            System.out.println("[Debug] Time limit exceeded (" + riddleSeconds + "s) → auto-fail.");
            return singleTransaction(riddleSeconds, false);
        }

        //check if detected landmark == currentTarget
        this.playerState.updateDetectedLandmark(); // Force update detected landmark before checking
        Landmark detectedLandmark = this.playerState.getDetectedLandmark();
        System.out.println("[Debug] Current target: " + this.currentTarget.getName() + " (ID: " + this.currentTarget.getId() + ")");
        System.out.println("[Debug] Detected landmark: " + (detectedLandmark != null ? detectedLandmark.getName() + " (ID: " + detectedLandmark.getId() + ")" : "null"));
        
        // for the sake of robustness!
        // if not detected landmark, front end does not allow submission button anyway
        if (detectedLandmark == null) {
            //if no landmark detected, current wrong count + 1
            System.out.println("[Debug] No landmark detected, wrong count + 1");
            return singleTransaction(riddleSeconds,false);
        }
        
        //if detected landmark is not within attemptsByLandmarkId
        if (detectedLandmark != null && !this.attemptsByLandmarkId.containsKey(detectedLandmark.getId())) {
            System.out.println("[Debug] Detected landmark not in target pool, wrong count + 1");
            return singleTransaction(riddleSeconds,false);
        } else {
            //if it's within attemptsByLandmarkId
            System.out.println("[Debug] Detected landmark is in target pool, checking if correct...");
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

    public Landmark selectNextTarget() {
        // select Nearest for MVP
        if (!this.playerState.isGameFinished()) { 
            // Check if target pool is empty
            if (getUnsolvedLandmarks().isEmpty()) {
                System.out.println("[Debug] Target pool is empty, marking game as finished");
                this.playerState.setGameFinished();
                this.currentTarget = null;
                this.puzzleManager.storeUserGameRoundStatistics();
                return null;
            }
            
            System.out.println("[Debug] Target pool before selection: " + getUnsolvedLandmarks().keySet());
            System.out.println("[Debug] Target pool size: " + getUnsolvedLandmarks().size());
            
            Landmark last = this.currentTarget;
            if (last == null){
                double playerLat = playerState.getPlayer().getLatitude();
                double playerLng = playerState.getPlayer().getLongitude();
                this.currentTarget = selectNearestTo(playerLat, playerLng);
            } else {
                this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
            }
            
            System.out.println("[Debug] Selected target: " + (this.currentTarget != null ? this.currentTarget.getName() + " (ID: " + this.currentTarget.getId() + ")" : "null"));
            
            //Generate Riddle only if target is found
            if (this.currentTarget != null) {
                String riddle = this.puzzleManager.getRiddleForLandmark(this.currentTarget.getId());
                this.currentTarget.setRiddle(riddle);
                
                // Force update detected landmark when target changes
                System.out.println("[Debug] Target changed, updating detected landmark...");
                this.playerState.updateDetectedLandmark();
                Landmark newDetected = this.playerState.getDetectedLandmark();
                System.out.println("[Debug] New detected landmark: " + (newDetected != null ? newDetected.getName() + " (ID: " + newDetected.getId() + ")" : "null"));
            }
    
        } else {    
            System.out.println("[Debug] Game is finished, no more targets");
            // puzzleManager.pausePuzzleTimer(currentTarget);
            this.currentTarget = null;
        }
    
        return this.currentTarget;
    }
    

    private boolean singleTransaction(long riddleSeconds, Boolean isCorrect){
        // Only proceed if game has not finished (i.e., game is active)
        if (this.playerState.isGameFinished()) {
            System.out.println("[Debug] Game is finished, ignoring transaction");
            return false;
        }
        
        if (isCorrect == false){
            this.attemptsByLandmarkId.put(this.currentTarget.getId(), this.attemptsByLandmarkId.get(this.currentTarget.getId()) - 1);
            System.out.println("[Debug] Current target attempts remaining: " + this.attemptsByLandmarkId.get(this.currentTarget.getId()));
            
            // Check if current target has reached 0 attempts
            if (this.attemptsByLandmarkId.get(this.currentTarget.getId()) <= 0) {
                System.out.println("[Debug] Current target has reached 0 attempts remaining");

                //pool removal and update rating (User & currentTarget)
                String lmid = this.currentTarget.getId();
                this.attemptsByLandmarkId.remove(lmid);
                eloCalculator.updateRating(lmid, riddleSeconds, isCorrect);

                // add to solved landmark as wrong
                this.solvedLandmarks.put(this.currentTarget.getId(), isCorrect);
                
                System.out.println("[Debug] Target pool after removing current target: " + this.attemptsByLandmarkId.keySet());
                System.out.println("[Debug] Target pool size after removal: " + this.attemptsByLandmarkId.size());
                
                // Check if this was the last target
                if (checkAndHandleGameEnd()) {
                    return isCorrect; // Game finished
                } else {
                    return isCorrect; // Continue with next target
                }
            }
            return isCorrect;
        } else {
            System.out.println("[Debug] Answer is correct!");
            System.out.println("[Debug] Target pool before removal: " + this.attemptsByLandmarkId.keySet());
            
            //pool removal and update rating (User & currentTarget)
            String lmid = this.currentTarget.getId();
            this.attemptsByLandmarkId.remove(lmid);
            eloCalculator.updateRating(lmid, riddleSeconds, isCorrect);
            
            System.out.println("[Debug] Target pool after removal: " + this.attemptsByLandmarkId.keySet());
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
            System.out.println("[Debug] Target pool is empty - game finished");
            this.playerState.setGameFinished();
            this.currentTarget = null;
            this.puzzleManager.storeUserGameRoundStatistics();
            return true; // Game finished
        } else {
            System.out.println("[Debug] Target pool not empty, selecting next target");
            selectNextTarget();
            return false; // Continue with next target
        }
    }

    private Landmark selectNearestTo(double refLat, double refLng) {
        System.out.println("[Debug] Trying to select from: " + getUnsolvedLandmarks().keySet());
        return getUnsolvedLandmarks().entrySet().stream()
            .map(entry -> gameDataRepository.findLandmarkById(entry.getKey()))
            .filter(landmark -> landmark != null)
            .min((l1, l2) -> {
                double d1 = GeoUtils.distanceInMeters(refLat, refLng, l1.getLatitude(), l1.getLongitude());
                double d2 = GeoUtils.distanceInMeters(refLat, refLng, l2.getLatitude(), l2.getLongitude());
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    public boolean answerCorrect(Landmark detectedLandmark) {
        if (playerState.getDetectedLandmark() == null || currentTarget == null) return false;
        
        boolean isCorrect = detectedLandmark.getId().equals(this.currentTarget.getId());
        System.out.println("[Debug] Comparing detected ID: " + detectedLandmark.getId() + " with target ID: " + this.currentTarget.getId() + " = " + isCorrect);
        
        return isCorrect;
    }

    public boolean isGameFinished() {
        if (this.playerState.isGameFinished()) {
            return true;
        }
        return this.attemptsByLandmarkId != null && this.attemptsByLandmarkId.isEmpty();
    }

    // ==================== Getters & Setters ====================

    public Map<String, Object> getCurrentTarget() {
        // If game is finished, don't try to select new targets
        if (this.playerState.isGameFinished()) {
            System.out.println("[Debug] Game is finished, returning null for current target");
            return null;
        }

        if (this.attemptsByLandmarkId == null || this.attemptsByLandmarkId.isEmpty()) {
            System.out.println("[Debug] Target pool empty. Game should be finished.");
            return null;
        }
        
        // If current target is null, try to select next target
        if (this.currentTarget == null) {
            System.out.println("[Debug] Current target is null, attempting to select next target");
            this.currentTarget = selectNextTarget();
        }

        if (this.currentTarget == null) {
            System.out.println("[Debug] No current target available.");
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", this.currentTarget.getId());
        result.put("name", this.currentTarget.getName());
        result.put("attemptsLeft", this.attemptsByLandmarkId.get(this.currentTarget.getId()));
        result.put("riddle", this.currentTarget.getRiddle());
        return result;
    }

    public String getUserId() {
        return userId;
    }

    public PlayerStateManager getPlayerState() {
        return this.playerState;
    }

    public Map<String, Integer> getUnsolvedLandmarks() {
        return this.attemptsByLandmarkId;
    }
}


