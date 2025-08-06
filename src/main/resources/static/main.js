  // ========== Global Variables ==========
  let userId = null;
  let city = 'Cork';

  let geoWatchId = null;


  let dragStart = null;  // Stores the starting point of mouse drag

  let playerCone = null;
  const spanDeg = 60;
  const coneRadiusMeters = 250;

  let playerMarker = null;  // The player's arrow marker on the INIT_MAP
  const icon = L.icon({     // Custom icon for player marker
    iconUrl: 'arrow.png',
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  });

  const landmarkMap = new Map();

  let playerCoord = null;    // Normal Player Coordination;
  let playerAngle = null;       // Normal PlayerAngle for realtime update
  let testPlayerAngle = null;   // Test Player facing angle

  let searchCircle = null;  // Circle representing search radius

  let countdownSeconds = 1800; // 30 minutes
  let countdownInterval = null;
  let countdownStartTimestamp = null;

  let roundStarted = false;

  let currentTargetCoord = null;
  let currentTargetId = null;

  let isCalibrating = false;
let calibrationPoints = [];
let calibratedAngleOffset = null;  // Calibrated angle offset

  // const LOCAL_HOST = "http://localhost:8443";   // Backend base URL
  const LOCAL_HOST = "https://3efed167c322.ngrok-free.app"  // Ngrok
  const ADMIN_TEST_COORD = L.latLng(51.8940, -8.4902);
  const INIT_MAP = L.map('map');  // Initialize INIT_MAP centered at UCC for test admin

  // ========== DOM Elements ==========
  const loginBtn = document.getElementById('login-btn');
  const registerBtn = document.getElementById('register-btn');
  const logoutBtn = document.getElementById('logout-btn');
  const startBtn = document.getElementById('start-round-btn');
  const submitBtn = document.getElementById('submit-answer-btn');
  const radiusSlider = document.getElementById('radius-slider');
  const calibrationBtn = document.getElementById('calibration-btn')
  let sliderRadius = parseFloat(radiusSlider.value);  // Current value of slider

  // ========== Helper Functions ==========

  // Update calibration status display function
  function updateCalibrationStatus() {
    const statusElement = document.getElementById('calibration-info');
    if (statusElement) {
      if (calibratedAngleOffset !== null) {
        const currentAngle = playerAngle || 0;
        let absoluteAngle = (calibratedAngleOffset + currentAngle) % 360;
        // Ensure angle is in 0-360 range
        if (absoluteAngle < 0) absoluteAngle += 360;
        statusElement.innerHTML = `
          <div style="color: green;">✓ Calibrated: ${calibratedAngleOffset.toFixed(1)}°</div>
          <div style="color: blue;">Device Angle: ${currentAngle.toFixed(1)}°</div>
          <div style="color: orange;">ViewCone Angle: ${absoluteAngle.toFixed(1)}°</div>
        `;
      } else {
        statusElement.innerHTML = '<div style="color: red;">Not calibrated</div>';
      }
    }
  }

  // Clean localStorage garbage data function
  function cleanLocalStorage() {
    // Keep important keys including calibration data
    const keysToKeep = ['userId', 'username', 'role', 'calibratedAngleOffset'];
    const currentValues = {};
    
    // Backup values to keep
    keysToKeep.forEach(key => {
      const value = localStorage.getItem(key);
      if (value !== null) {
        currentValues[key] = value;
      }
    });
    
    // Clear localStorage
    localStorage.clear();
    
    // Restore important values
    Object.keys(currentValues).forEach(key => {
      localStorage.setItem(key, currentValues[key]);
    });
    
    console.log("[Frontend] LocalStorage cleaned, kept:", Object.keys(currentValues));
  }

  // ========== Init Game Space ==========

  function ensureUserId() {
    let userId = localStorage.getItem('userId');
    
    if (!userId) {
      userId = 'guest-' + crypto.randomUUID();
      localStorage.setItem('userId', userId);
      localStorage.setItem('role', "GUEST");
      console.log("[Frontend][Init] Generate guest userId: ", userId);
    }
    
    // Restore previously saved calibrated angle offset
    const savedCalibration = localStorage.getItem("calibratedAngleOffset");
    if (savedCalibration && calibratedAngleOffset === null) {
      calibratedAngleOffset = parseFloat(savedCalibration);
      console.log("[Frontend][Init] Restored calibrated angle offset:", calibratedAngleOffset);
    }
    
    // Update calibration status display
    setTimeout(() => updateCalibrationStatus(), 100);
  }

  function updateAuthUI() {
    const isGuest = localStorage.getItem('role') === 'GUEST';

    loginBtn.style.display = isGuest ? 'inline-block' : 'none';
    registerBtn.style.display = isGuest ? 'inline-block' : 'none';
    logoutBtn.style.display = isGuest ? 'none' : 'inline-block';
    
    // radiusSlider.disable = roundStarted ? true : false;
    document.getElementById('radius-ui').style.display = isGuest || roundStarted ? 'none' : 'block';
    
    // Start Round button requirements: non-guest user && game not started && (calibrated OR admin)
    const isAdmin = localStorage.getItem('role') === 'ADMIN';
    const canStartRound = !isGuest && !roundStarted && (calibratedAngleOffset !== null || isAdmin);
    startBtn.disabled = !canStartRound;
  }
    
  function initMap() {
    const userRole = localStorage.getItem('role');
    updateAuthUI();
    
    if(userRole === "ADMIN"){
      INIT_MAP.setView(ADMIN_TEST_COORD, 17);
      console.log('[Frontend][Init] Admin INIT_MAP initialized at UCC');
      playerCoord = ADMIN_TEST_COORD; // re-ensuring fetching default coord
      //update testPlayer Position
      playerMarker = L.marker(ADMIN_TEST_COORD, {icon}).addTo(INIT_MAP);
      setTimeout(() => {
        initGame();
      }, 100);
    } else {
      // RESET GUEST/PLAYER COORD
      fetchPlayerCoord();
    }
  }


  function initGame(){
    ensureUserId();
    let angle;
    if (localStorage.getItem('role') === 'ADMIN') {
      angle = testPlayerAngle;
    } else if (calibratedAngleOffset !== null) {
      // Calibrated mode: send absolute angle (same as ViewCone angle)
      angle = (calibratedAngleOffset + (playerAngle ?? 0.0)) % 360;
      // Ensure angle is in 0-360 range
      if (angle < 0) angle += 360;
    } else {
      // Uncalibrated mode: send raw device angle
      angle = playerAngle;
    }

    const requestBody = {
      latitude: playerCoord.lat,
      longitude: playerCoord.lng,
      angle: angle,
      spanDeg: spanDeg,
      coneRadiusMeters: coneRadiusMeters,
      city: city,
      userId: localStorage.getItem('userId'),
    }

    console.log(`[Frontend][InitGame] Sending angle: ${angle}, calibratedOffset: ${calibratedAngleOffset}, rawPlayerAngle: ${playerAngle}`);
    console.log(`[Frontend][InitGame] Calculation: ${calibratedAngleOffset} + ${playerAngle} = ${calibratedAngleOffset + (playerAngle ?? 0.0)} -> normalized: ${angle}`);

    fetch(LOCAL_HOST + '/api/game/init-game', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(requestBody)
    })
    .then(res => res.json())
    .then(data => {
      console.log("[Frontend] InitGame response:", data);
    
      if (!data.landmarks || data.landmarks.length === 0) {
        console.warn("[Frontend] No landmarks received");
        return;
      }

      landmarkMap.forEach(polygon => {
        INIT_MAP.removeLayer(polygon);
      });
      landmarkMap.clear();

      data.landmarks.forEach(lm => {
        const polygon = L.polygon(lm.coordinates, {
          color: "grey",
          fillOpacity: 0.3
        }).addTo(INIT_MAP);

        polygon.options.name = lm.name; 
        landmarkMap.set(lm.id, polygon);  
      });
    })
  }

  // ========== Authentication ==========

  function register(username, password){
    fetch(LOCAL_HOST + '/api/auth/register', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({username, password})
    })
    .then(res => {
      if (res.status == 400) {
        alert('Username already exists.');
        throw new Error('Username already exists.');
      }
      return res.text();
    })
    .then(msg => {
      console.log('[Frontend][Registration]: Register successful: ', msg);
      // Also clean localStorage after successful registration
      cleanLocalStorage();
      login(username, password)
    })
    .catch(err => {
      console.log('[Frontend][Registration]: Register failed: ', err);
      alert('Register failed')
    })
  }

  function login(username, password) {
    fetch(LOCAL_HOST + '/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    })
    .then(res => {
      if (!res.ok) throw new Error('[Frontend][Registration]Invalid username or password.');
      return res.json()
    })
    .then(userInfo => {
      // Clean localStorage garbage data on successful login
      cleanLocalStorage();
      
      localStorage.setItem('userId', userInfo.userId);
      localStorage.setItem('username', userInfo.username);
      localStorage.setItem('role', userInfo.role);

      alert('Logged In.');
      updateAuthUI();
      
      // Reset game state
      resetGameToInit();
    })
  }

  function logout(){
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('calibratedAngleOffset');  // Clear calibration on logout
    localStorage.setItem('role', "GUEST");
    
    // Reset calibration state
    calibratedAngleOffset = null;
    isCalibrating = false;
    
    resetGameToInit(); 
    initMap();
    updateAuthUI(); 
    setupInteractions();
    alert('Logout successful.')
  }

  // ========== Player Movement ==========

  // ========== Calibration ==========


  function startCalibration() {
    playerAngle = null;
    rawAngleAtCalibration = null;
    alert("Starting Calibration! Please lay flat your device. And walk 2~3 meters.");
    isCalibrating = true;
    calibrationPoints = [];  // Reset calibration points array
    calibratedAngleOffset = null;  // Reset previous calibration result

    if (geoWatchId !== null) {
      navigator.geolocation.clearWatch(geoWatchId);
    }

    geoWatchId = navigator.geolocation.watchPosition(
      position => {
        const latlng = L.latLng(position.coords.latitude, position.coords.longitude);

        if (calibrationPoints.length === 0) {
          calibrationPoints.push(latlng);
          return;
        }

        const last = calibrationPoints[calibrationPoints.length - 1];
        const distance = last.distanceTo(latlng);

        if (distance > 0.4) { // move at least 1 meter
          calibrationPoints.push(latlng);
          console.log(`[Calibration] Point ${calibrationPoints.length}:`, latlng);

          if (calibrationPoints.length >= 5) {
            // stop sampling
            navigator.geolocation.clearWatch(geoWatchId);
            geoWatchId = null;
            finishCalibration();
          }
        }
      },
      error => {
        alert("Calibration failed. Please check authority.");
        console.error("[Calibration] GPS error:", error);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 1000
      }
    );
  }

  function finishCalibration() {
    if (calibrationPoints.length < 2) {
      alert("Calibration Fail! Please walk a few more step");
      startCalibration();
      return;
    }
  
    const a = calibrationPoints[0];
    const b = calibrationPoints[calibrationPoints.length - 1];
    const pathBearing = calculateAngle(a, b);
  
    calibratedAngleOffset = pathBearing % 360;
    localStorage.setItem("calibratedAngleOffset", calibratedAngleOffset.toString());
  
    const distance = a.distanceTo(b);
    alert(`Calibration Success!
    Points collected: ${calibrationPoints.length}
    Distance moved: ${distance.toFixed(1)}m
    Start: ${a.lat.toFixed(6)}, ${a.lng.toFixed(6)}
    End: ${b.lat.toFixed(6)}, ${b.lng.toFixed(6)}
    Calculated angle: ${Math.round(pathBearing)}°
    Math details: dy=${(b.lat - a.lat).toFixed(8)}, dx=${(b.lng - a.lng).toFixed(8)}`);
  
    updateCalibrationStatus();
    initMap();
    setupInteractions();
  
    if (window.DeviceOrientationEvent) {
      const oneTimeHandler = function (event) {
        let heading;
        if (event.webkitCompassHeading !== undefined) {
          heading = event.webkitCompassHeading;
        } else if (event.alpha !== null) {
          heading = -event.alpha % 360;
        }
  
        if (heading !== undefined) {
          rawAngleAtCalibration = heading;
          playerAngle = 0;
          console.log(`[Calibration] Forced heading: ${heading.toFixed(1)}°, playerAngle reset to 0`);
          updatePlayerViewCone(0);
          updateCalibrationStatus();
        } else {
          console.warn("[Calibration] Failed to read heading");
        }
  
        window.removeEventListener('deviceorientationabsolute', oneTimeHandler, true);
      };
  
      window.addEventListener('deviceorientationabsolute', oneTimeHandler, true);
    }
  
    isCalibrating = false;
    initGame();
    drawRadiusCircle();
  }
  

  function initOrientationListener() {
    if (
      typeof DeviceOrientationEvent !== 'undefined' &&
      typeof DeviceOrientationEvent.requestPermission === 'function'
    ) {
      // iOS
      DeviceOrientationEvent.requestPermission()
        .then(state => {
          if (state === 'granted') {
            window.addEventListener('deviceorientationabsolute', handleOrientation, true);
          }
        })
        .catch(console.error);
    } else {
      // Android
      window.addEventListener('deviceorientationabsolute', handleOrientation, true);
    }
  }


  function handleOrientation(event) {
    let heading;
  
    if (event.webkitCompassHeading !== undefined) {
      heading = event.webkitCompassHeading;
    } else if (event.alpha !== null) {
      heading = -event.alpha % 360;
    }
  
    if (heading !== undefined && !isCalibrating) {
      if (rawAngleAtCalibration !== null) {
        playerAngle = (heading - rawAngleAtCalibration + 360) % 360;
      } else {
        playerAngle = heading;
      }
  
      updatePlayerViewCone(playerAngle);
  
      if (playerCoord && playerCoord.lat != null) {
        if (playerMarker) {
          playerMarker.setRotationAngle(calibratedAngleOffset !== null ? 0 : playerAngle);
        }
      }
  
      const angleDisplay = document.getElementById('angle-display');
      if (angleDisplay) {
        angleDisplay.innerText = `Angle: ${playerAngle.toFixed(2)}°`;
      }
    }
  }
  

  // ======== Game Mechanism ========

  function startRound() {
    let role = localStorage.getItem('role');
    if (role === 'GUEST') {
      console.log("[Frontend] Please Log in First");
      return;
    }

    if (calibratedAngleOffset === null && role !== 'ADMIN') {
      alert("Please calibrate your device first before starting the round.");
      return;
    }

    if (!playerCoord) {
      alert("Waiting for location... Please allow GPS or wait a few seconds.");
      return;
    }

    // Ensure game is initialized
    console.log("[Frontend] Ensuring game is initialized before starting round...");
    initGame();

    roundStarted = true;
    radiusSlider.disabled = true;

    if (searchCircle){
      INIT_MAP.removeLayer(searchCircle);
      searchCircle = null;
    }

    // Calculate correct angle: use calibrated angle in calibrated mode
    let effectiveAngle;
    if (localStorage.getItem('role') === 'ADMIN') {
      effectiveAngle = testPlayerAngle ?? 0.0;
    } else if (calibratedAngleOffset !== null) {
      // Calibrated mode: send absolute angle (same as ViewCone angle)
      effectiveAngle = (calibratedAngleOffset + (playerAngle ?? 0.0)) % 360;
      // Ensure angle is in 0-360 range
      if (effectiveAngle < 0) effectiveAngle += 360;
    } else {
      effectiveAngle = playerAngle ?? 0.0;  
    }

    const requestBody = {
      userId: localStorage.getItem('userId'),
      latitude: playerCoord?.lat ?? 0.0,
      longitude: playerCoord?.lng ?? 0.0,
      angle: effectiveAngle,
      radiusMeters: sliderRadius
    };

    console.log("[Frontend] startRound request:", requestBody);

    fetch(LOCAL_HOST + '/api/game/start-round', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    .then(res => {
      if (res.status === 404) {
        return res.text().then(errorText => {
          resetGameToInit();
          if (errorText.includes("No target available")) {
            alert("No landmarks found in the selected area. Try increasing your search radius or move to a different location.");
          } else {
            alert("Session expired. Please start a new game.");
          }
          throw new Error(errorText);
        });
      }
      return res.json();
    })
    .then(data => {

      if (searchCircle){
        INIT_MAP.removeLayer(searchCircle);
        searchCircle = null;
      }

      document.getElementById('target-info').innerText = data.name;
      document.getElementById('chances-left').style.display = 'block';
      document.getElementById('chances-left').innerText = `Remaining Attempts: ${data.attemptsLeft}`;
      document.getElementById('riddle-box').innerText = data.riddle ? data.riddle : "(No riddle)";

      setTimeout(() => {
        document.getElementById('countdown-timer').style.display = 'block';
        startCountdown();
      }, 100);
      
      // Only change button state after successful round start
      startBtn.style.display = 'inline-block';
      startBtn.innerText = "Finish Round";
      startBtn.onclick = finishRound; 
      submitBtn.disabled = false;
      submitBtn.style.display = 'inline-block';
    })
    .catch(err => {
      console.error("[Frontend] startRound error:", err);
      alert("Failed to start round.");
      drawRadiusCircle();
      
      // Reset button state on failure
      roundStarted = false;
      radiusSlider.disabled = false;
      startBtn.innerText = "Start Round";
      startBtn.onclick = startRound;
      submitBtn.disabled = true;
    });
  }

  function submitAnswer() {
    const secondsUsed = stopCountdown();
    if (secondsUsed == null) {
      alert("Time not running!");
      return;
    }

    // Log current angle information for debugging
    let currentAngle;
    if (localStorage.getItem('role') === 'ADMIN') {
      currentAngle = testPlayerAngle;
    } else if (calibratedAngleOffset !== null) {
      currentAngle = (calibratedAngleOffset + (playerAngle ?? 0.0)) % 360;
      if (currentAngle < 0) currentAngle += 360;
    } else {
      currentAngle = playerAngle;
    }
    console.log(`[Frontend][Submit] Current angle: ${currentAngle.toFixed(2)}°, Position: ${playerCoord?.lat.toFixed(6)}, ${playerCoord?.lng.toFixed(6)}`);

    const requestBody = {
      userId: localStorage.getItem('userId'),
      secondsUsed: secondsUsed,
      currentAngle: currentAngle,
      latitude: playerCoord?.lat ?? 0.0,
      longitude: playerCoord?.lng ?? 0.0
    };

    fetch(LOCAL_HOST + '/api/game/submit-answer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    .then(res => res.json())
    .then(data => {
      console.log("[Frontend] Submit result:", data);
      alert(data.message);

      if (data.gameFinished) {
        resetGameToInit();
        return;
      }

      // Only color the landmark if it was solved (correct answer) OR if target changed (meaning previous target was exhausted)
      const currentTargetName = document.getElementById('target-info').innerText;
      const shouldColorLandmark = data.isCorrect || (data.target && data.target.name !== currentTargetName);
      
      if (shouldColorLandmark) {
        for (const [id, polygon] of landmarkMap.entries()) {
          if (polygon && polygon.options && polygon.options.name === currentTargetName) {
            polygon.setStyle({ color: 'blue' });
            break;
          }
        }
      }

      // ===== update target info =====
      if (data.target) {
        document.getElementById('target-info').innerText = data.target.name;
        document.getElementById('chances-left').style.display = 'block';
        document.getElementById('chances-left').innerText = `Remaining Attempts: ${data.target.attemptsLeft}`;
        document.getElementById('riddle-box').innerText = data.target.riddle ? data.target.riddle : "(No riddle)";

        setTimeout(() => {
          startCountdown();
        }, 100);
      }
    })
    .catch(err => {
      console.error("[Frontend] Failed to submit answer:", err);
      alert("Failed to submit answer.");
    });
  }

  function finishRound() {
    fetch(LOCAL_HOST + '/api/game/finish-round', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: localStorage.getItem('userId')
      })
    })
    .then(res => res.json())
    .then(data => {
      alert(data.message);
      resetGameToInit();
    })
    .catch(err => {
      console.error("[Frontend] Failed to finish round:", err);
      alert("Finish round failed.");
    });
  }

  function resetGameToInit() {
    roundStarted = false;
    radiusSlider.disabled = false;
    countdownSeconds = 1800;
    countdownStartTimestamp = null;


    if (playerMarker) {
      INIT_MAP.removeLayer(playerMarker);
      playerMarker = null;
    }

    if (playerCone) {
      INIT_MAP.removeLayer(playerCone);
      playerCone = null;
    }

    if (searchCircle) {
      INIT_MAP.removeLayer(searchCircle);
      searchCircle = null;
    }

    // landmarkMap.forEach(polygon => {
    //   INIT_MAP.removeLayer(polygon);
    // });
    // landmarkMap.clear();

    document.getElementById('countdown-timer').textContent = "";
    document.getElementById('countdown-timer').style.display = 'none';
    document.getElementById('target-info').innerText = "(No target yet)";
    document.getElementById('chances-left').style.display = 'none';
    document.getElementById('riddle-box').innerText = "";

    submitBtn.disabled = true;
    startBtn.disabled = false;
    startBtn.innerText = "Start Round";
    startBtn.onclick = startRound;

    // landmarkMap.forEach(polygon => {
    //   polygon.setStyle({ color: 'darkgrey' });
    // });

    initMap();
    updatePlayerViewCone(playerAngle || 0);
    drawRadiusCircle();
  }

  // ========== Test Player Movement ==========

  function updateTestPlayerPosition(lat, lng, angle) {
    ensureUserId();
    playerCoord = L.latLng(lat, lng); // update frontend latlng
    const userId = localStorage.getItem("userId");
    return fetch(LOCAL_HOST + "/api/game/update-position", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({userId: userId, latitude: lat, longitude: lng, angle: angle})
    })
    .then(res => res.text())
    .then(msg => {
      console.log("[Frontend] Position updated", msg);
      if (localStorage.getItem('role"') === 'GUEST') {
        console.log("[Frontend] Guest user - position updated but no session created");
      }
    });
  }


  // ========== Tool Functions ==========

  function startCountdown() {
    clearInterval(countdownInterval);   
    countdownSeconds = 1800;            
    countdownStartTimestamp = Date.now();

    const timerDisplay = document.getElementById('countdown-timer');

    function updateTimer() {
      const minutes = Math.floor(countdownSeconds / 60);
      const seconds = countdownSeconds % 60;
      timerDisplay.textContent = `Remaining Time:, ${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

      if (countdownSeconds <= 0) {
        clearInterval(countdownInterval);
        timerDisplay.textContent = "Time's up!";
        onCountdownFinish();
      }

      countdownSeconds--;
    }

    updateTimer(); 
    countdownInterval = setInterval(updateTimer, 1000);
  }

  function stopCountdown() {
    clearInterval(countdownInterval);

    if (!countdownStartTimestamp) return null;

    const now = Date.now();
    const elapsedMs = now - countdownStartTimestamp;
    const elapsedSeconds = Math.floor(elapsedMs / 1000);

    console.log("[Frontend] Time used:", elapsedSeconds, "seconds");
    return elapsedSeconds;
  }

  function drawRadiusCircle() {
    if (localStorage.getItem("role") === "GUEST") return;
    if (!roundStarted){
      if (!searchCircle) {
        searchCircle = L.circle(playerCoord, {
          radius: sliderRadius,
          color: 'blue',
          fillColor: '#cce5ff',
          fillOpacity: 0.3
        }).addTo(INIT_MAP);
      } else {
        searchCircle.setLatLng(playerCoord);
        searchCircle.setRadius(sliderRadius);
      } 
    }
  }

  function updatePlayerViewCone(playerAngle) {

    if (!playerCoord || (playerCoord.lat == null || playerCoord.lng == null)) return;

    let coneAngle;  // Cone angle in map coordinate system (地图坐标系中的锥形角度)
    
    if (localStorage.getItem('role') === 'ADMIN'){
      // Admin mode: cone follows testPlayerAngle, map doesn't rotate
      coneAngle = testPlayerAngle;
    } else {
      if(calibratedAngleOffset !== null){
        coneAngle = (calibratedAngleOffset + playerAngle) % 360;
        // Ensure angle is in 0-360 range
        if (coneAngle < 0) coneAngle += 360;
        console.log(`[Debug ViewCone] calibratedOffset: ${calibratedAngleOffset.toFixed(1)}°, playerAngle: ${playerAngle.toFixed(1)}°, finalCone: ${coneAngle.toFixed(1)}°`); 
      } else {
        // Uncalibrated mode: cone follows device angle directly
        coneAngle = playerAngle || 0;
      }
    }

    if (coneAngle == null) return;

    // Apply map rotation
    
    const resolution = 20;  
    const startAngle = coneAngle - spanDeg / 2;
    const endAngle = coneAngle + spanDeg / 2;

    const conePoints = [[playerCoord.lat, playerCoord.lng]];  

    for (let i = 0; i <= resolution; i++) {
      const angleDeg = startAngle + (i / resolution) * (endAngle - startAngle);
      const angleRad = angleDeg * Math.PI / 180;

      // Earth coordinate correction: approximately 1° latitude ≈ 111.32 km, longitude needs adjustment based on latitude
      const latOffset = (coneRadiusMeters * Math.cos(angleRad)) / 111320; 
      const lngOffset = (coneRadiusMeters * Math.sin(angleRad)) / (111320 * Math.cos(playerCoord.lat * Math.PI / 180));

      const lat = playerCoord.lat + latOffset;
      const lng = playerCoord.lng + lngOffset;

      conePoints.push([lat, lng]);
    }

    if (playerCone) {
      INIT_MAP.removeLayer(playerCone);
    }

    playerCone = L.polygon(conePoints, {
      color: 'orange',
      fillColor: 'orange',
      fillOpacity: 0.35,
      weight: 1
    }).addTo(INIT_MAP);
    
    updateCalibrationStatus();
  }


  function fetchPlayerCoord() {
    // reference 
    // Real time location tracker app on leafletjs || HTML5 geolocation || Tekson
    // https://www.youtube.com/watch?v=8KX4_4NK7ZY

    if (localStorage.getItem('role') === 'ADMIN') return;

    if (!navigator.geolocation) {
      alert('Geolocation is not supported on this device.');
      return;
    }

    if (geoWatchId !== null) {
      navigator.geolocation.clearWatch(geoWatchId);
    }

    let firstUpdate = true;
    let accuracyCircle = null;

    geoWatchId = navigator.geolocation.watchPosition(
      position => {
        playerCoord = L.latLng(position.coords.latitude, position.coords.longitude);
        const accuracy = position.coords.accuracy;

        updatePlayerViewCone(playerAngle || 0);
        drawRadiusCircle(); 

        if (!playerMarker) {
          playerMarker = L.marker(playerCoord, { icon }).addTo(INIT_MAP);
        } else {
          playerMarker.setLatLng(playerCoord);
        }

        // tracking
        if (firstUpdate) {
          INIT_MAP.setView(playerCoord, 17);
          firstUpdate = false;
          
          // Initialize game session for regular players after first location update
          if (localStorage.getItem('role') === 'PLAYER') {
            setTimeout(() => {
              console.log("[Frontend] Initializing game session for player at:", playerCoord);
              initGame();
            }, 500);
          }
        } else {
          // pan always follows player
          INIT_MAP.panTo(playerCoord);
        }

        // accuracy circle
        if (accuracyCircle) {
          accuracyCircle.setLatLng(playerCoord);
          accuracyCircle.setRadius(accuracy);
        } else {
          accuracyCircle = L.circle(playerCoord, {
            radius: accuracy,
            color: 'green',
            fillColor: '#c2f0c2',
            fillOpacity: 0.2
          }).addTo(INIT_MAP);
        }

      },
      error => {
        console.error('[Geolocation Error]', error);
        alert('Location tracking failed: ' + error.message);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 1000
      }
    );
  }

  function setupInteractions() {

    INIT_MAP.off('mousedown');
    INIT_MAP.off('mousemove');
    INIT_MAP.off('mouseup');

    if (localStorage.getItem('role') === 'ADMIN') {

      INIT_MAP.on('mousedown', function(e) {
        dragStart = e.latlng;
        INIT_MAP.dragging.disable();

        if (!playerMarker) {
          playerMarker = L.marker(dragStart, {
            icon: icon,
            rotationAngle: 0,
            rotationOrigin: 'center center'
          }).addTo(INIT_MAP);
        } else {
          playerMarker.setLatLng(dragStart);
          playerMarker.setRotationAngle(0);
        }
      });

      INIT_MAP.on('mousemove', function(e) {
        if (!dragStart || !playerMarker) return;
        const angle = calculateAngle(dragStart, e.latlng);
        playerMarker.setRotationAngle(angle);
      });

      INIT_MAP.on('mouseup', function(e) {
        if (!dragStart || !playerMarker) return;

        const angle = calculateAngle(dragStart, e.latlng);
        const lat = dragStart.lat;
        const lng = dragStart.lng;

        playerLat = lat;
        playerLng = lng;
        testPlayerAngle = angle;


        updateTestPlayerPosition(lat, lng, testPlayerAngle).then(() => {
          updatePlayerViewCone(testPlayerAngle);
          drawRadiusCircle();
        });

        dragStart = null;
        INIT_MAP.dragging.enable();
      });

      console.log("[Frontend] Admin interactions enabled");
    } else {
      // normal tracking which is a pain in the butt
      console.log("[Frontend] Admin interactions disabled");
    }
  }


  function calculateAngle(start, end) {
    const dy = end.lat - start.lat;  
    const dx = end.lng - start.lng;  
    
    // Calculate bearing: measured clockwise from north
    const theta = Math.atan2(dx, dy);
    let angle = theta * (180 / Math.PI);
    
    // Ensure angle is in 0-360 degree range
    if (angle < 0) angle += 360;
    
    return angle;
  }


  // ========== Main ==========

  document.addEventListener('DOMContentLoaded', () => {

    window.cleanLocalStorage = cleanLocalStorage;

    ensureUserId();
    updateAuthUI();  // Ensure UI state is updated on initialization
    setupInteractions();
    
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(INIT_MAP);
    
    initMap();
    initOrientationListener();
    
    registerBtn.addEventListener('click', () => {
      const username = prompt('Enter Username: ');
      const password = prompt('Enter Password: ');
      register(username, password);
    });

    loginBtn.addEventListener('click', () => {
      const username = prompt('Enter username:');
      const password = prompt('Enter password:');
      login(username, password);
    });

    logoutBtn.addEventListener('click', () => {
      logout();
    });

    radiusSlider.addEventListener('input', ()=> {
      sliderRadius = parseFloat(radiusSlider.value);
      if(!roundStarted) {
        drawRadiusCircle();
      }
    });

    startBtn.addEventListener('click', () => {
      startRound();
    });

    submitBtn.addEventListener('click', () => {
      submitAnswer();
    });

    calibrationBtn.addEventListener('click', () => {
      startCalibration();
    })

  })