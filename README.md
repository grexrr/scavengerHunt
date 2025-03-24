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
│   ├── GameStateManager.java          # Controls state transitions (init, puzzle, success, end)
│   ├── Landmark.java                  # Landmark entity (location, name, riddle, etc.)
│   ├── LandmarkManager.java           # Loads/saves/switches landmarks (including random target selection)
│   ├── RiddleManager.java             # Riddle management (loaded locally or from Python)
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