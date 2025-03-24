# scavengerHunt

**Scavenger Hunt(Un-named)** is an interactive Java-based location puzzle game enhanced by a Python backend. The game challenges players to find real-world landmarks based on riddles. Players navigate a rendered map, receive clues, and interact using keyboard and mouse controls. The system evaluates user actions and determines success based on spatial accuracy and orientation.

### Module Overview

```perl
scavenger_hunt/                        # Project root directory (Java main program)
├── App.java                           # Entry point, initializes all modules
│
├── map/                               # Map rendering and interaction
│   ├── MapEngine.java                 # Loads OSM map, initializes layers
│   ├── PlayerPointer.java             # Player arrow, controls position/orientation/movement
│   ├── AreaSelector.java              # Press 'A' + mouse to create circular area (search zone)
│   └── DirectionArrow.java            # Arrow pointing to target landmark (navigation guide)
│
├── game/                              # Game logic management
│   ├── Player.java                    # Stores player's basic data; PlayerStateManager controls progression logic.
│   ├── PlayerStateManager.java        # Controls state transitions (init, puzzle, success, end)
│   ├── Landmark.java                  # Landmark entity (location, name, riddle, etc.)
│   ├── LandmarkManager.java           # Loads/saves/switches landmarks (including random target selection)
│   ├── RiddleManager.java             # Riddle management provides higher level of Gamer Management of the
│   │                                  # previous modules(loaded locally or from Python)
│   └── AnswerEvaluator.java           # Checks if player correctly identifies the landmark (angle + distance + interaction)
│
├── interaction/                       # Player input and interaction
│   ├── InputController.java           # Keyboard and mouse listener (movement, button operations, etc.)
│   ├── ButtonAHandler.java            # Handles pressing and dragging with 'A' to create search area
│   ├── ButtonBHandler.java            # Handles 'B' key to enter "answering" phase
│   └── FacingChecker.java             # Checks if player is facing the target landmark (angle tolerance)
│
├── comms/                             # Communication with Python backend
│   ├── RiddleAPIClient.java           # Sends HTTP requests to fetch riddles
│   └── APIResponseParser.java         # Parses and formats data returned from Python (JSON)
│
├── utils/                             # Utility classes
│   ├── GeoUtils.java                  # Geographic calculations (distance, angle)
│   └── TimerUtils.java                # Timers, long-press detection, etc.
│
├── config/                            # Configurations and constants
│   └── Constants.java                 # Radius thresholds, angle tolerance, API endpoints, etc.
│
├── assets/                            # Resource files (local data)
│   ├── riddles.json                   # Locally pre-defined riddles (with landmark name and riddle text)
│   └── landmarks.json                 # Predefined landmark data (name and coordinates)
│
├── requirements.txt                   # Python dependencies (Flask, OpenAI, etc.)
└── riddle_api/                        # Python backend module (standalone service)
    ├── app.py                         # Main entry point: Flask/FastAPI service
    ├── openai_client.py               # Encapsulates OpenAI API calls
    ├── local_riddle_loader.py         # Loads riddles from riddles.json (MVP)
    ├── utils.py                       # Shared utility functions (e.g. prompt templates)
    └── config.py                      # Python-side configuration, such as API keys
```

This module demonstrates full-stack coordination between a Java desktop application and a lightweight Python server using RESTful APIs, with a strong focus on geospatial interaction and puzzle-solving mechanics.


### Dev Log

#### Mar. 24 2025
**Remark: The log is scratch written in Chinese and generated with help of ChatGPT. It WILL NOT be used for thesis paper submission.** 

- Project bootstrapped using Spring Boot 3.4.4 with Maven (Java 17)
- Initialized base package: `com.scavengerhunt`
- Added `.game` module:
  - **Player**: encapsulates core player data, including location, orientation, solved landmarks, and game status
  - **PlayerStateManager**: manages player's game state transitions and interactions, such as current target updates and game completion
  - **Landmark**: defines the landmark data model (ID, name, location, riddle, solved status)
  - **LandmarkManager**: handles landmark filtering, selection, and navigation logic during a game session
- Added `.utils` module:
  - **GeoUtils**: performs geographical distance calculations (Haversine formula) used for radius checks and proximity comparison
- Added `.data` module:
  - **LocalGameDataManager**: MVP-stage repository that loads hardcoded landmarks and saves player progress via standard I/O or console

##### Project Progress Summary (up to Mar. 24, 2025)

This phase focused on **validating module communication, state transitions, and data flow**, in preparation for the MVP milestone. Specifically, the goal was to ensure that the core gameplay logic — player progress tracking, landmark selection, riddle sequencing — could function end-to-end even without a graphical interface.

##### Modules Designed and Tested:
- `Player` / `PlayerStateManager`: encapsulates user position, solved history, and current puzzle state
- `Landmark` / `LandmarkManager`: supports filtering landmarks by radius, selecting the next target by proximity, and separating loaded landmarks (`localLandmarks`) from dynamically updated game targets (`unsolvedLandmarks`)
- `GeoUtils`: utility for distance-based filtering
- `LocalGameDataManager`: temporary I/O controller to load landmark data and save solved history
- **TestRunner** (Pipeline testing data transmission between Module): simulated game loop that loads data → selects puzzle → marks solved → selects next

##### Data Flow Simulated:
1. Player initializes at fixed lat/lng coordinates
2. Landmarks are loaded from a stubbed source (`LocalGameDataManager`)
3. The player defines a circular search area (simulated radius)
4. A filtered puzzle pool is created → the nearest landmark is selected
5. Player “solves” the puzzle → LandmarkManager updates `unsolvedLandmarks`
6. The next target is chosen based on proximity
7. If no further landmarks remain, the game ends and the state is saved

#### Next Step: MVP UI Integration

Having validated the core gameplay logic in a console-based simulation, the next step is to begin **UI-layer integration**, where player inputs and spatial movement will drive visual feedback and real-time interactions.

##### Goals:
- Implement a minimal graphical UI
- Display player position and surrounding landmarks
- Simulate area selection (A key → drag to create search radius)
- Show directional arrow to guide player toward current target
- Enable answer submission (B key) with orientation validation
- Sync UI feedback with existing game state logic (PlayerStateManager & LandmarkManager)