package com.scavengerhunt.game;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.repository.LandmarkRepository;
import com.scavengerhunt.utils.GeoUtils;

public class GameSession {
    
    private PlayerStateManager playerState;
    private LandmarkManager landmarkManager;
    private PuzzleManager puzzleManager;
    private LandmarkRepository landmarkRepository;

    private Landmark currentTarget;
    private Map<String, Integer> currentTargetPool; 
    // private List<Landmark> solvedLandmarks = new ArrayList<>(); 
    // public List<String> detectableLandmarkIds;

    private String userId;

    public GameSession(PlayerStateManager playerState, LandmarkManager landmarkManager, String userId, PuzzleManager puzzleManager) {
        this.playerState = playerState;
        this.landmarkManager = landmarkManager;
        this.userId = userId;
        this.puzzleManager = puzzleManager;
    }

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

        // init answering space
        this.detectableLandmarkIds = landmarkRepository.findAllId();

        // init candidate map
        List<String> candidateLandmarksId= landmarkManager.getRoundLandmarksIdWithinRadius(lat, lng, radiusMeters);
        this.currentTargetPool = candidateLandmarksId.stream()
            .collect(Collectors.toMap(landmark -> landmark, landmark -> 0));

        this.currentTarget = null;

        System.out.println("[Session] New game round started.");
    }

    public boolean submitCurrentAnswer() {
        if (this.currentTarget == null) {
            System.out.println("[Session] No current Target.");
            return false;
        };

        Integer currentCount = this.currentTargetPool.get(this.currentTarget);

        while (currentCount <= 3){

        }
        return false;

        // if (checkAnswerCorrect(this.currentTarget) == true) {
        //     selectNextTarget();

        //     if (isGameFinished()) {
        //         playerState.setGameFinished();
        //     }
        //     return true;
        // }
        // return false;
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
        return getUnsolvedLandmarks().stream()
            .min((l1, l2) -> {
                double d1 = GeoUtils.distanceInMeters(refLat, refLng, l1.getLatitude(), l1.getLongitude());
                double d2 = GeoUtils.distanceInMeters(refLat, refLng, l2.getLatitude(), l2.getLongitude());
                return Double.compare(d1, d2);
            })
            .orElse(null);
    }

    public boolean checkAnswerCorrect(Landmark landmark) {
        // Future integration: answer evaluator + epistemic 
        return true;  // default true for mvp
    }

    public boolean isGameFinished() {
        return getUnsolvedLandmarks().isEmpty();
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

    public List<Landmark> getUnsolvedLandmarks() {
        return this.currentTargetPool.stream()
            .filter(lm -> !solvedLandmarks.contains(lm))
            .collect(Collectors.toList());
    }

    public String getUserId() {
        return userId;
    }

    public PlayerStateManager getPlayerState() {
        return this.playerState;
    }
}


