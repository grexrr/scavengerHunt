# scavengerHunt

**Scavenger Hunt(Un-named)** is an interactive Java-based location puzzle game enhanced by a Python backend. The game challenges players to find real-world landmarks based on riddles. Players navigate a rendered map, receive clues, and interact using keyboard and mouse controls. The system evaluates user actions and determines success based on spatial accuracy and orientation.

### Module Overview (Updating)

```perl
scavenger_hunt/                        # Project root directory (Java backend)
├── App.java                           # Application entry point; initializes Spring Boot and core modules
│
├── controller/                        # REST API controllers exposed to frontend
│   ├── GameRestController.java        # Main game controller: handles position updates, round start, answer submission, and target retrieval
│   ├── AuthController.java            # Handles user registration, login, and logout
│   └── AdminController.java           # Admin utility endpoints for bulk landmark/user insert or database cleanup
│
├── game/                              # Core game logic and session state
│   ├── GameSession.java               # Manages per-user session state: player position, current target, solved landmarks
│   ├── PlayerStateManager.java        # Updates and tracks player's location, orientation, and game status
│   ├── LandmarkManager.java           # Loads landmarks and filters them based on radius from player
│   ├── Player.java                    # Data model for player (latitude, longitude, angle, ID, nickname)
│   ├── Landmark.java                  # Data model for landmarks (ID, name, riddle, coordinates)
│
├── repository/                        # MongoDB data access layer
│   ├── LandmarkRepository.java        # Spring Data interface for accessing landmarks collection
│   ├── UserRepository.java            # Spring Data interface for accessing users collection
│   └── GameDataRepository.java        # Aggregates access to both UserRepository and LandmarkRepository, provides unified game data interface
│
├── utils/                             # Utility classes
│   └── GeoUtils.java                  # Geolocation math helpers: distance calculation, angle computation
│
├── frontend/                          # Static frontend resources served by Spring Boot
│   ├── index.html                     # Main web page with Leaflet map UI
│   └── main.js                        # Full game client logic: map interaction, auth, player control, target display
│
├── assets/                            # Static data assets (shared by Java and Python)
│   ├── riddles.json                   # Fallback riddles if API is unavailable
│   └── landmarks.json                 # Optional pre-defined landmarks
│
├── requirements.txt                   # Python backend dependencies
└── riddle_api/                        # Python REST backend (LLM-based riddle generator)
    ├── app.py                         # Entry point using Flask or FastAPI
    ├── riddle_generator.py            # Uses OpenAI API to generate riddles based on landmark metadata
    ├── landmark_generator.py          # Queries OSM (via Overpass API) and processes landmarks
    ├── local_riddle_loader.py         # Loads riddles from local JSON
    ├── utils.py                       # Prompt templates and format helpers
    └── config.py                      # OpenAI keys, model settings, and flags

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


##### Static Resource Structure

```
resources/
└── static/
    ├── index.html
    ├── main.js
    └── player_click_init.js
```

##### Design Notes

- Global-scope JS functions selected over ES Modules for MVP simplicity
- State flag `gameInitialized` is used to guard single-init behavior
- Plan to decouple `UIController` from direct instantiation in `GameRestController` and move to injected singleton later

---

#### Apr. 20 2025

**Goal: Implement dynamic search radius interaction using Leaflet; allow player to visually select a search area before starting a riddle round.**

- Enhanced `main.js` with interactive radius selection logic:
  - Player can freely click to move their location on the map at any time
  - A blue circular overlay (`L.circle`) reflects the current search radius
  - A slider (`#radius-slider`) dynamically adjusts the radius in real time
  - Player’s coordinates and radius are updated together when initiating a round

- Added `start-round` control panel to the UI:
  - HTML includes a hidden div (`#radius-ui`) with a slider and a "Start Round" button
  - Once player is initialized, the control panel is displayed
  - Clicking "Start Round" sends the player’s current location and selected radius to the backend

- Backend:
  - Updated `GameRestController` to accept `POST /api/game/start-round` with `StartRoundRequest` DTO
  - Request includes latitude, longitude, angle, and radius
  - The backend updates player position (`updatePlayerPosition`) before applying radius filter (`applySearchArea`)
  - Log statements print filtered landmarks and the selected target for verification
  - Added new endpoint `GET /api/game/target` to return current target landmark

- Target display (front-end):
  - After starting a round, frontend fetches `/api/game/target` to retrieve current target info
  - Target name, riddle, and coordinates are shown in the UI info box
  - A new marker is placed on the map to indicate the target location
  - Logic ensures `/target` is only fetched after round is successfully started (avoids 404)

##### JavaScript Logic Summary

```
main.js
├── updatePlayerPositionOnMap(lat, lng)
│   ├── Updates marker
│   ├── Updates circle
│   └── Displays coordinates
├── attachInitHandler(map, callback)
│   └── Handles initial and subsequent clicks
├── startButton.addEventListener(...)
│   ├── Sends payload to /start-round
│   └── Then fetches /api/game/target and updates target UI
```

##### Design Notes

- Circle visualization is kept in sync with both player movement and slider value
- Start-round endpoint is responsible for setting both position and search area
- Target is only fetched after the round starts, ensuring up-to-date information
- UIController is now managed by Spring (`@Component`) and injected via constructor, ensuring session state is preserved between requests


---

#### Apr. 21 2025

**Goal: Implement full puzzle loop logic with proximity detection and answer submission; remove UIController and consolidate session logic.**

- Refactored backend controller:
  - `UIController` fully removed; all logic merged into `GameRestController`
  - `GameRestController` now manages `GameSession` and `GameDataRepo` directly
  - All endpoints operate on internally stored `session` object

- Added `/api/game/submit-answer` endpoint:
  - Handles current puzzle submission
  - Returns `"All riddles solved!"` if no next target
  - Otherwise, server logs and returns `"Next target: {name}"`

- Enhanced proximity interaction on the frontend:
  - Removed confirm popup for proximity
  - Added a persistent `Submit Answer` button next to “Start Round”
  - Button is hidden by default and only shown when the player is within 50m of the current target
  - If the player moves out of range, the button disappears again

- Target transition after answer:
  - Submitting an answer automatically fetches the next target
  - Target marker and UI update without reloading the page
  - If all riddles are completed, a finished message is shown and interaction is disabled

- Minor enhancements:
  - `searchRadiusCircle` is removed after starting the round and will not reappear on subsequent player moves
  - Target marker is now correctly removed and redrawn on each puzzle switch
  - Target proximity is re-evaluated every time the player moves

##### JavaScript Logic Summary

```
main.js
├── updatePlayerPositionOnMap(lat, lng)
│   ├── Moves marker
│   ├── Adjusts circle if it exists
│   ├── Syncs coordinates with backend
│   └── Triggers proximity check to current target
├── checkProximityToTarget(lat, lng)
│   ├── Shows submit button if within 50m of target
│   └── Hides button if player moves away
├── submitAnswer()
│   ├── Sends POST to /submit-answer
│   ├── On success, fetches next target and updates UI
│   └── If all riddles solved, disables interaction and shows final message
```

##### Design Notes

- `searchRadiusCircle` is now strictly a pre-round visual aid and removed after round starts
- `targetMarker` is stored and replaced cleanly across puzzle transitions
- State variables like `currentTargetLatLng` and `window.answerPromptShown` ensure UI logic consistency
- Entire puzzle loop (init → move → start → solve → next) is now complete and seamless

--- 

#### Apr. 29 2025

**Goal: Build a complete player authentication system with MongoDB integration; prepare frontend for modular refactor.**

---

- **Refactored backend authentication system:**
  - Implemented `AuthController` with endpoints `/register`, `/login`, `/logout`
  - `register` generates a unique `playerId` (UUID) and stores it in MongoDB
  - `login` validates `username/password` and returns the corresponding `playerId`
  - `logout` placeholder implemented (future extensibility)
  - Created `User` entity:
    - Annotated with `@Document("users")`
    - Used `playerId` as the Mongo `_id`
  - Created `UserRepository` extending `MongoRepository<User, String>`:
    - Added method `findByUsername(String username)`

- **MongoDB setup completed:**
  - Integrated Spring Boot MongoDB connection via `application.properties`
  - Successfully registered and stored users into the MongoDB `scavengerhunt` database
  - Verified database contents through Docker Mongo shell

- **Frontend authentication flow established:**
  - Added `register()`, `login()`, and `logout()` functions in `main.js`
  - Successfully stored `playerId` in `localStorage` upon login
  - Dynamic UI update:
    - If logged in, hide Register/Login, show Logout
    - If logged out, show Register/Login, hide Logout
  - After successful registration, automatic login flow implemented

- **Frontend HTML structure adjustments:**
  - Created a dedicated `auth-controls` section for Register/Login/Logout buttons
  - `DOMContentLoaded` now binds all button actions
  - Preserved Leaflet.js map initialization

---

##### Design Notes

- `User` entity now cleanly mapped to MongoDB, using `playerId` as the document `_id`
- Frontend authentication state is driven entirely by localStorage
- Registration flow directly transitions into login without user re-entry
- Codebase now cleanly separates **data model (User)**, **data access (UserRepository)**, and **API layer (AuthController)**
- MongoDB acts as the single source of truth for player authentication data
- Frontend ready for modularization in the next phase (separating map control, auth control, game control)

---

#### Apr. 30 2025

**Goal: Integrate MongoDB-based landmark storage and session-based game state management; unify all data access through `GameDataRepository` and prepare backend for round logic (start/submit/target).**

---

- **Backend MongoDB integration for landmark system:**
  - Created `Landmark` model with `@Document("landmarks")`, supporting `name`, `riddle`, `latitude`, `longitude`
  - Replaced hardcoded landmark list with MongoDB as persistent source
  - Defined `LandmarkRepository` extending `MongoRepository<Landmark, String>`
  - Added `AdminController` with endpoints:
    - `/insert-landmarks`: Insert predefined landmarks
    - `/clear-landmarks`: Wipe landmark collection
    - `/insert-users`: Insert test admin user

- **Game data architecture refactor:**
  - Introduced `GameDataRepository` as the **unified backend access layer** for both `UserRepository` and `LandmarkRepository`
  - `GameDataRepository` now handles:
    - Loading all landmarks (used by `LandmarkManager`)
    - Future: filtering by region or radius (planned extension)
    - Loading and updating user solved landmark list
  - `GameRestController` now delegates all DB interactions through `GameDataRepository`

- **Game session management:**
  - `GameSession` manages player-specific state:
    - Player location
    - Solved landmark tracking
    - Current target and local landmark pool
  - `sessionMap<String, GameSession>` used to track per-player sessions in `GameRestController`
  - `update-position` endpoint implemented:
    - Supports **both logged-in and guest users**
    - Automatically creates a new `GameSession` if one does not exist

- **Frontend logic refinement:**
  - Introduced `ensurePlayerId()` to support guest play via auto-generated UUID stored in `localStorage`
  - `updatePlayerPosition()` sends `playerId`, `lat`, `lng`, and `angle` to backend
  - Added `playerMarker` logic to update Leaflet map marker only after response confirmation
  - Modularized logic to better support upcoming `/start-round` and `/submit-answer` endpoints

---

##### Design Notes

- Full separation between `LandmarkManager` (game logic) and `GameDataRepository` (data access) is now in place
- `GameRestController` acts as stateful per-player dispatcher; avoids misuse of shared data by isolating sessions
- Frontend supports anonymous play (auto-created `guest-UUID`) while maintaining same API call structure as authenticated users
- Data source consolidation allows MongoDB to act as single source of truth for **both** authentication and gameplay records
- Current foundation is now ready for implementation of `/start-round`, `/get-target`, `/submit-answer` puzzle flow

---

#### Apr. 30 2025

**Goal: Integrate MongoDB-based landmark storage and session-based game state management; unify all data access through `GameDataRepository` and prepare backend for round logic (start/submit/target).**


* **Backend MongoDB integration for landmark system:**

  * Created `Landmark` model with `@Document("landmarks")`, supporting `name`, `riddle`, `latitude`, `longitude`
  * Replaced hardcoded landmark list with MongoDB as persistent source
  * Defined `LandmarkRepository` extending `MongoRepository<Landmark, String>`
  * Added `AdminController` with endpoints:

    * `/insert-landmarks`: Insert predefined landmarks
    * `/clear-landmarks`: Wipe landmark collection
    * `/insert-users`: Insert test admin user

* **Game data architecture refactor:**

  * Introduced `GameDataRepository` as the **unified backend access layer** for both `UserRepository` and `LandmarkRepository`
  * `GameDataRepository` now handles:

    * Loading all landmarks (used by `LandmarkManager`)
    * (Planned extension) Filtering landmarks by region or radius
    * Loading and updating user solved landmark lists
  * `GameRestController` now delegates all DB interactions through `GameDataRepository`

* **Game session management:**

  * `GameSession` manages player-specific state:

    * Player location
    * Solved landmark tracking
    * Current target and local landmark pool
  * `sessionMap<String, GameSession>` tracks per-player sessions in `GameRestController`
  * Implemented `update-position` endpoint:

    * Supports **both logged-in and guest users**
    * Automatically creates new `GameSession` if one does not exist

* **Frontend logic refinement:**

  * Introduced `ensurePlayerId()` to support guest play with auto-generated UUID stored in `localStorage`
  * `updatePlayerPosition()` sends `playerId`, `lat`, `lng`, and `angle` to backend
  * Added `playerMarker` logic to update Leaflet map marker only after receiving response
  * Modularized logic to better support upcoming `/start-round` and `/submit-answer` endpoints

---

##### Design Notes

* Fully separated `LandmarkManager` (game logic) and `GameDataRepository` (data access layer)
* `GameRestController` serves as a stateful per-player dispatcher, isolating sessions and preventing shared data misuse
* Frontend supports anonymous play while using the same backend API structure as authenticated users
* MongoDB now serves as the single source of truth for both authentication and gameplay records
* The system foundation is ready for the full puzzle flow implementation (`/start-round`, `/get-target`, `/submit-answer`)

---

#### May. 7 2025

**Goal: Finalize round-based gameplay flow with full puzzle lifecycle: player location/angle updates, target selection, answer submission, and end-of-round detection. Added directional arrow logic and angle-based validation.**

* **Leaflet-based player direction logic:**

  * Integrated `leaflet.rotatedMarker` to support player orientation rendering
  * Player drag interaction now sets both position and facing angle:

    * `mousedown` stores drag start
    * `mousemove` continuously rotates marker based on drag vector
    * `mouseup` finalizes location and angle, then posts to `/update-position`
  * Implemented `calculateAngle(start, end)` for consistent rotation (0° = North, clockwise)

* **Frontend angle + proximity check:**

  * `checkProximityAndDirection()` compares player facing to target angle
  * Shows `Submit Answer` button only when:

    * Distance to target < 50m
    * Angle difference ≤ 30°
  * UI updates in real-time on drag or rotation

* **Endpoint behavior cleanup:**

  * `/start-round`: initializes session and filters local landmark pool; does **not** select a target
  * `/first-target`: selects and returns first unsolved target if `currentTarget == null`
  * `/submit-answer`:

    * If `currentTarget` is null, triggers first target selection
    * On valid submission, advances to next target
    * Returns `"All riddles solved!"` string when no targets remain

* **Frontend flow updates:**

  * Removed usage of deprecated `/next-target` endpoint
  * After starting round, frontend calls `/first-target` to display first puzzle
  * `Submit` button triggers `/submit-answer`, handles either:

    * JSON (next target)
    * String (final message)
  * `target-info` is updated accordingly; proximity is re-evaluated

* **Backend session logic:**

  * `GameSession`:

    * Maintains `currentTarget`, `solvedLandmarks`, and player position
    * `selectNextTarget()` chooses closest unsolved landmark
    * `submitCurrentAnswer()` adds solved landmark and checks for completion
  * `GameRestController`:

    * `/submit-answer` uses `Map<String, String>` for simplicity
    * Automatically selects first target if none exists
    * Uses `serializeLandmark()` for consistent JSON return
  * `first-target` improved to avoid 404 by auto-selecting if `currentTarget == null`

##### Notes

* Round flow is now: `start-round → first-target → submit-answer (→ repeat)`
* Facing angle logic entirely frontend-driven
* No redundant state on frontend—player and target vectors are computed per interaction
* System supports both guest and logged-in users with unified logic

---

#### **May. 17 2025**

**Goal: Integrate puzzle generation pipeline with gameplay flow; introduce `PuzzleManager` to decouple riddle retrieval from frontend logic. Finalize backend riddle management and database structure.**

* **PuzzleManager integration:**

  * Introduced `PuzzleManager` Java class to decouple riddle selection logic from `GameRestController`
  * Provided method `getRiddleForLandmark(String landmarkId)` that:

    * Queries MongoDB `riddles` collection using `RiddleRepository`
    * Returns the first available `"mysterious"` style riddle for the given landmark
    * Falls back to a default message if no riddle is found
  * Enabled injection of `PuzzleManager` via `@PostConstruct` in `GameRestController`

* **Riddle model and database setup:**

  * Defined `Riddle.java` to match MongoDB schema:

    * `landmarkId`, `style`, `source`, `content`, `metadata`
  * Created `RiddleRepository` interface extending `MongoRepository`
  * Populated MongoDB `riddles` collection via Python script `riddle_generator.py`, using GPT-based generation with multi-style support
  * Ensured one-to-many mapping between landmarks and riddles

* **Frontend riddle support:**

  * No changes required in frontend JavaScript or HTML
  * `/first-target` and `/submit-answer` endpoints now return `target.riddle` automatically via backend merging logic
  * Frontend displays updated riddle content in the UI without requiring structural changes

* **Backend session update:**

  * `GameRestController`:

    * Augmented target response map to include puzzle text via `PuzzleManager`
    * Ensured puzzle delivery is consistent across target initialization and submission steps

* **Development workflow enhancements:**

  * `riddle_generator.py` refactored to support:

    * MongoDB-based landmark loading
    * Chainable calls: `.load().generate().saveToDB().saveToFile()`
    * GPT-4-based riddle generation with style control
  * Aligned Python data structure with Java entity schema for seamless interoperability

##### Notes

* All riddles now served dynamically from database; `Landmark.riddle` field deprecated
* Puzzle retrieval logic fully modular via `PuzzleManager`, enabling future support for:

  * Style preference
  * Difficulty adaptation
  * User personalization
* Frontend logic remains clean and API-driven, with riddle display abstracted from data source

---

#### **Jun. 28-29 2025**

引入ELO机制完成玩家/地标评分系统，因此产生对数据结构/后端的影响需处理。草案如下。


|模块|子任务|是否准备就绪|备注|
|---|---|---|---|
|**MongoDB 索引**|为 `geometry` 字段添加 `2dsphere` 索引|✅ 是|若未建索引，只需 `db.landmarks.createIndex({ geometry: "2dsphere" })`|
|**后端结构变更**|`Landmark` 增加 `GeoJsonPolygon geometry` 映射|✅ 是|Spring Data MongoDB 支持 GeoJSON，需用 `org.springframework.data.mongodb.core.geo.GeoJsonPolygon`|
|**GameRestController**|- `startRound`：根据玩家位置查找所有**geometry范围内**的地标- 返回地标列表用于前端高亮- 选中后进入谜题答题流程|⚠️ 需实现|原逻辑只匹配 `centroid`|
|**答题判断**|- `submitAnswer` 接收 `landmarkId`- 判断是否是本轮允许作答的地标- 正误判定（字符串比对 / 模糊匹配）- 更新 user/landmark 的 rating/uncertainty|⚠️ 需实现|包括计时、尝试次数上限等逻辑|
|**Elo 逻辑注入**|Java 实现简化版 `elo_calculator_demo.py` 中逻辑|⚠️ 待移植|若你希望我先给 Java 版 Elo 实现草稿，也可以安排|
|**metadata 调用**|根据 `landmarkId` 找到 `landmark_metadata` 并附带 `meta.description` 返回|⚠️ 可选|可放入 `submitAnswer` 返回体中|
|**前端逻辑**|- 地标轮廓→点击→选中提交- 显示谜题、尝试次数、倒计时- 答题后展示科普信息（来自 metadata）|⚠️ 部分已有|目前仅支持质心点与按钮点击，需扩展选中方式与 UI|


更新后游戏主逻辑

1. 允许答题: 玩家视角扇面 ∩ 任一 landmark.geometry ≠ ∅
2. 答题流程: 玩家在“允许答题”状态下点击 Submit Answer，由后端判断正误
3. 尝试上限: 每题最多 3 次机会, 30分钟为答题时间上限
4. 更新 Elo: 玩家答题正确 or 已尝试 3 次


更新Player类渲染JTS PlayerCone用于检测player视线是否与地标轮廓线存在交集

更新GeoUtils工具类，提供player对landmark的检测逻辑：(略，GPT补充)
**强调说明，目前用算法检测最优landmark作为玩家作答的检测是MVP需要的逻辑因此不甚严谨**
但这并非本项目需要重点展示的部分。未来希望可以通过AR检测，这个部分的内容放在future works篇描述。