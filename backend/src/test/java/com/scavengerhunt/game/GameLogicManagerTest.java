package com.scavengerhunt.game;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scavengerhunt.client.LandmarkProcessorClient;
import com.scavengerhunt.client.PuzzleAgentClient;
import com.scavengerhunt.model.Landmark;
import com.scavengerhunt.model.PersistedGameSession;
import com.scavengerhunt.repository.AnswerTransactionRecordRepository;
import com.scavengerhunt.repository.GameDataRepository;

public class GameLogicManagerTest {

    @Mock private GameDataRepository mockGameDataRepo;

    @Mock private LandmarkProcessorClient mockLandmarkProcessorClient;

    @Mock PuzzleAgentClient mockPuzzleAgentClient;

    @Mock AnswerTransactionRecordRepository mockAnswerTransactionRecordRepo;

    private PersistedGameSession mockSession;
    private GameLogicManager mockGame;

    //test landmarks
    private Landmark glucksman = new Landmark(
        "686fe2fd5513908b37be306d",
        "Glucksman Gallery",
        "Cork",
        51.894741757894735,
        -8.490317963157894);

    Landmark quad = new Landmark(
        "686fe2fd5513908b37be3071",
        "The Quad",
        "Cork",
        51.89372202222222,
        -8.492224097916667);

    Landmark boole = new Landmark(
        "6895327b04e4917e0d875789",
        "Boole Library",
        "Cork",
        51.89285984,
        -8.491245088);

    @BeforeEach
    public void setup() {

        MockitoAnnotations.openMocks(this);

        this.mockSession = new PersistedGameSession("mock-session-123", "alex123", "Cork");

        this.mockGame = new GameLogicManager(
            this.mockSession,
            this.mockGameDataRepo,
            this.mockLandmarkProcessorClient,
            this.mockPuzzleAgentClient,
            this.mockAnswerTransactionRecordRepo,
            30
        );
    }
}
