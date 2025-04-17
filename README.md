# scavengerHunt

**Scavenger Hunt(Un-named)** is an interactive Java-based location puzzle game enhanced by a Python backend. The game challenges players to find real-world landmarks based on riddles. Players navigate a rendered map, receive clues, and interact using keyboard and mouse controls. The system evaluates user actions and determines success based on spatial accuracy and orientation.

### Module Overview (Updating)

```perl
scavenger_hunt/                        # Project root directory (Java main program)
├── App.java                           # Entry point, initializes all modules
│
├── map/                               # Map rendering and UI interaction (Leaflet-based, via WebView)
│   ├── MapEngine.java                 # Loads HTML-based Leaflet map (via WebView or browser), initializes base layers
│   ├── PlayerPointer.java             # Player marker + orientation arrow (updated via JS bridge)
│   ├── AreaSelector.java              # Press 'A' + mouse to create circular search zone
│   ├── DirectionArrow.java            # Arrow pointing to current target landmark
│   └── MapUIStateManager.java         # (NEW) Manages visible markers/layers, syncs player/landmark state to UI
│
├── game/                              # Core game logic and player state
│   ├── Player.java                    # Player data model: location, orientation, solved landmark IDs
│   ├── PlayerStateManager.java        # Controls player movement and solved record tracking
│   ├── Landmark.java                  # Landmark entity (ID, location, riddle, name)
│   ├── LandmarkRepo.java              # Provides raw landmark access and spatial filtering (no state)
│   ├── PuzzleController.java          # Controls puzzle round state: current target, puzzle flow, solved checking
│   ├── GameSession.java               # (NEW) Top-level game wrapper that aggregates player, puzzle controller, and sync
│   ├── RiddleManager.java             # Provides riddles to PuzzleController (from local or API)
│   └── AnswerEvaluator.java           # Validates answer correctness (angle, distance, interaction)
│
├── interaction/                       # Handles player input and event triggers
│   ├── InputController.java           # Keyboard or mobile input adapter; triggers puzzle/game logic
│   ├── ButtonAHandler.java            # Triggers area selection (circle via 'A' + mouse)
│   ├── ButtonBHandler.java            # Submits current answer (via 'B')
│   └── FacingChecker.java             # Checks if player is facing the target landmark (angle threshold)
│
├── comms/                             # Handles communication with Python backend
│   ├── RiddleAPIClient.java           # Sends HTTP request to fetch riddles
│   └── APIResponseParser.java         # Parses JSON responses from Python
│
├── utils/                             # Utility modules
│   ├── GeoUtils.java                  # Geolocation helpers: Haversine, angle, radius checks
│   └── TimerUtils.java                # Long-press detection, timer control
│
├── config/                            # Configurable constants and thresholds
│   └── Constants.java                 # Radius/angle thresholds, API URLs, UI params, etc.
│
├── assets/                            # Static assets (used both by Java + frontend map)
│   ├── riddles.json                   # Local fallback riddles
│   ├── landmarks.json                 # Local landmark definitions
│   └── map.html                       # Leaflet map HTML (for WebView load)
│
├── requirements.txt                   # Python-side dependencies
└── riddle_api/                        # Python backend (lightweight REST API)
    ├── app.py                         # Flask/FastAPI entry point
    ├── openai_client.py               # Handles GPT-based riddle generation
    ├── local_riddle_loader.py         # Local JSON riddle manager
    ├── utils.py                       # Text templates and formatting
    └── config.py                      # Python config: keys, model parameters

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

##### Issues & Solution:
The module-oriented development approach has led to structural confusion issues. We have decided to adopt a **Domain-Driven Design** (DDD) + Use Case First development methodology for the next phase of work to see if it can alleviate these problems.


#### Next Step: MVP UI Integration

Having validated the core gameplay logic in a console-based simulation, the next step is to begin **UI-layer integration**, where player inputs and spatial movement will drive visual feedback and real-time interactions.

##### Goals:
- Implement a minimal graphical UI
- Display player position and surrounding landmarks
- Simulate area selection (A key → drag to create search radius)
- Show directional arrow to guide player toward current target
- Enable answer submission (B key) with orientation validation
- Sync UI feedback with existing game state logic (PlayerStateManager & LandmarkManager)


#### Mar. 29, 2025

##### Updated Design Notes

- Separated the pure data layer (`LandmarkRepo`) from the stateful game logic (`PuzzleController`)
- Introduced `MapUIStateManager` to sync internal state with frontend rendering
- Added `GameSession` as the bridge between logic and UI, encapsulating player and puzzle states
- Kept `PuzzleController` focused on single-round management: provides methods such as `submitAnswer()`, `getCurrentTarget()`, and `isFinished()`
- The UI layer communicates exclusively with `GameSession` or `GameUIController`

##### Frontend Strategy (Revised)

- The MVP map is rendered using Leaflet inside an HTML page, embedded through JavaFX WebView or opened in a browser window
- User interaction is handled through key/mouse/touch input passed to the backend
- Future extension paths:
  - Progressive Web App (PWA) for mobile browser compatibility
  - Embedding the WebView into a native Android/iOS app using frameworks such as Flutter, React Native, or Cordova
  - Full native mobile frontend rewrite for later phases (as needed)

##### Progress Recap

###### PuzzleController & LandmarkRepo Refinement
- Responsibilities clarified:
  - `PuzzleController` maintains round-specific state, such as the target pool and progress tracking
  - `LandmarkRepo` acts as a data provider with geospatial filtering, no longer maintains round state
- Achieved strict use-case alignment by avoiding logic duplication and state coupling

###### Session Layer Implementation
- `GameSession` introduced to orchestrate state between player, puzzle logic, and data
- Ensures a clean API surface for the frontend
- Facilitates long-term modularity and scalability

###### UI Prototype Integration
- `UIController` implemented as the first-layer interface between the frontend and logic
- Tested a terminal-based MVP flow:
  - Initializes a player
  - Applies a search radius to create the puzzle pool
  - Handles answer submission and target progression
  - Detects game completion cleanly


##### Insights from MVP Debugging

- `GameDataRepo` should evolve into a unified backend communication hub:
  - Manages not only landmark/riddle retrieval, but also future player data syncing
  - Will eventually integrate with a Python REST API backend

- Layer separation (Repo ↔ Controller ↔ Session ↔ UI) proved valuable for testability and logic clarity

##### Next Focus Areas

###### 1. Simulated Player Movement
- Add tools to manually or programmatically update player position
- Dynamically affect game state and puzzle logic
- Used to validate spatial correctness and navigation feedback

###### 2. Distance-Based Puzzle Validation
- Implement `evaluateCurrentTarget()` with geolocation checks
- Later enhance to include directional angle (facing logic)
- Might involve an `AnswerEvaluator` utility or similar abstraction

###### 3. Strengthen Frontend Visualization
- Decide and initiate the map rendering layer:
  - JavaFX + embedded Leaflet HTML
  - Or fully browser-based progressive web frontend
- Immediate goal: visualize player and landmarks on a map

---

##### Reflection

Although the game logic layer is solid, the UI brings substantial added complexity. Key challenges include:

- Interactive graphics (maps, players, arrows)
- User input handling (mouse and keyboard)
- Synchronizing state across logic and UI layers

However, once the map visualization is functional, additional game features will become significantly easier to build.

---

#### Apr. 17 2025

**Goal: Establish frontend-backend interaction using Leaflet-based map interface, replacing the prior CLI `TestRunner`. Focused on player initialization via map click and modular JavaScript structure.**


- Created `index.html` to host a full-screen Leaflet map, simulating player location input by clicking
- Designed and separated client logic into JS modules:
  - `player_click_init.js`: first-click event listener for player init, calling `/api/game/init`
  - `main.js`: application entrypoint, loads the map and mounts init handler
- Adopted **global function style** for frontend scripts for simple browser-based testing, avoiding ES module complexity

- Backend:
  - Added `GameRestController` with `@RestController` + `/api/game` prefix
  - Implemented `POST /api/game/init` endpoint with `PlayerInitRequest` DTO
  - Connected endpoint to `UIController.initGame(...)`, confirming Spring Boot logs display proper coordinates upon frontend interaction
  - Verified full chain: map click → fetch → POST JSON → session initialized

---

##### Static Resource Structure

```
resources/
└── static/
    ├── index.html
    ├── main.js
    └── player_click_init.js
```

##### Data Flow Verified

| Action | Trigger | Payload | Endpoint | Result |
|--------|---------|---------|----------|--------|
| Click map | Leaflet | `{lat, lng, angle}` | `POST /api/game/init` | Game session initialized |

---

##### Design Notes

- Global-scope JS functions selected over ES Modules for MVP simplicity
- State flag `gameInitialized` is used to guard single-init behavior
- Plan to decouple `UIController` from direct instantiation in `GameRestController` and move to injected singleton later

---

