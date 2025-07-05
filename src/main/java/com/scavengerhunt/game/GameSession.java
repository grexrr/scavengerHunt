package com.scavengerhunt.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.GameDataRepository;
import com.scavengerhunt.utils.EloUtils;
import com.scavengerhunt.utils.GeoUtils;

public class GameSession {
    private String userId;

    private GameDataRepository gameDataRepository;

    private PlayerStateManager playerState;
    private LandmarkManager landmarkManager;
    private PuzzleManager puzzleManager;
    private EloUtils eloManager;

    private Landmark currentTarget;
    private Map<String, Integer> targetPool; 
    private Map<String, Boolean> solvedLandmarks = new HashMap<>(); // only for frontend rendering

    private int maxWrongAnswer = 3;


    public GameSession(String userId, GameDataRepository gameDataRepository, PlayerStateManager playerState, LandmarkManager landmarkManager, PuzzleManager puzzleManager, EloUtils eloManager){
        this.userId = userId;
        this.gameDataRepository = gameDataRepository;
        this.playerState = playerState;
        this.landmarkManager = landmarkManager;
        this.puzzleManager = puzzleManager;
        this.eloManager = eloManager;
    }

    // public GameSession(PlayerStateManager playerState, GameDataRepository gam, String userId, PuzzleManager puzzleManager) {
    //     this.playerState = playerState;
    //     this.landmarkRepository 
    //     this.landmarkManager = new LandmarkManager(landmarkRepository, playerState.getPlayer().getCity());
    //     this.userId = userId;
    //     this.puzzleManager = puzzleManager;
    // }

    /** 
     * Core Function
     */

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

        // map the currect answer counter
        this.targetPool = candidateLandmarks.stream()
            .collect(Collectors.toMap(landmark -> landmark.getId(), landmark -> 0));


        // select the first target
        selectNextTarget();
        
        System.out.println("[Session] New game round started.");
    }

    public boolean submitCurrentAnswer() {
        if (this.currentTarget == null) {
            System.out.println("[Session] No current Target.");
            return false;
        };

        // check if current Target is answered more than 3 times
        if (this.targetPool.get(this.currentTarget.getId()) >= this.maxWrongAnswer){
            //get rid from target pool
            this.targetPool.remove(this.currentTarget.getId());
            // add to solved landmark
            this.solvedLandmarks.put(this.currentTarget.getId(), false);
            
            // updateELO() & select next target
            selectNextTarget();
            return false; // terminates
        }

        // check if detected landmark == currentTarget
        Landmark detectedLandmark = this.playerState.getDetectedLandmark();
        if (this.targetPool.get(detectedLandmark.getId()) == null) {
            //if detected landmark is not within TargetPool, current wrong count + 1
            this.targetPool.put(this.currentTarget.getId(), this.targetPool.get(this.currentTarget.getId()) + 1);
            return false;
        } else{
            //if it's within TargetPool
            if (answerCorrect(detectedLandmark)){
                //if correct
                this.targetPool.remove(this.currentTarget.getId());
                this.solvedLandmarks.put(this.currentTarget.getId(), true);
                
                // updateELO() & select next target
                selectNextTarget();
                return true;
            } else {
                this.targetPool.put(this.currentTarget.getId(), this.targetPool.get(this.currentTarget.getId()) + 1);
                return false;
            }
        }
    }

    public Landmark selectNextTarget() {
        // select Nearest for MVP
        if (!isGameFinished()) {
            Landmark last = this.currentTarget;
            if (last == null){
                double playerLat = playerState.getPlayer().getLatitude();
                double playerLng = playerState.getPlayer().getLongitude();

                this.currentTarget = selectNearestTo(playerLat, playerLng);
            } else {
                this.currentTarget = selectNearestTo(last.getLatitude(), last.getLongitude());
            }
            
            //Generate Riddle
            String riddle = puzzleManager.getRiddleForLandmark(this.currentTarget.getId());
            this.currentTarget.setRiddle(riddle);

        } else {    
            this.currentTarget = null;
        }
        return this.currentTarget;
    }

    /** 
     * Helper Functions
     */

    private Landmark selectNearestTo(double refLat, double refLng) {
        return getUnsolvedLandmarks().entrySet().stream()
            .map(entry -> gameDataRepository.findLandmarkById(entry.getKey()).orElse(null))
            .filter(landmark -> landmark != null)
            .min((l1, l2) -> {
                double d1 = GeoUtils.distanceInMeters(refLat, refLng, l1.getLatitude(), l1.getLongitude());
                double d2 = GeoUtils.distanceInMeters(refLat, refLng, l2.getLatitude(), l2.getLongitude());
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    public boolean answerCorrect(Landmark detectedLandmark) {
        return detectedLandmark.getId() == this.currentTarget.getId();
    }

    public boolean isGameFinished() {
        return this.playerState.isGameFinished();
    }

    /** 
     * Getter & Setter
     */

    public Landmark getCurrentTarget() {
        if (this.currentTarget == null) {
            this.currentTarget = selectNextTarget();
        }
        return this.currentTarget;
    }
    public String getUserId() {
        return userId;
    }

    public PlayerStateManager getPlayerState() {
        return this.playerState;
    }

    public Map<String, Integer> getUnsolvedLandmarks() {
        return this.targetPool;
    }
}


