// ========== Global Variables ==========
let userId = null;
let city = 'Cork';

let geoWatchId = null;

let dragStart = null;  // Stores the starting point of mouse drag

let playerCone = null;
const spanDeg = 30;
const coneRadiusMeters = 50;

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


// const LOCAL_HOST = "http://localhost:8443";   // Backend base URL
const LOCAL_HOST = "https://d30c7f9234cf.ngrok-free.app"  // Ngrok
const ADMIN_TEST_COORD = L.latLng(51.8940, -8.4902);
const INIT_MAP = L.map('map');  // Initialize INIT_MAP centered at UCC for test admin

// ========== DOM Elements ==========
const loginBtn = document.getElementById('login-btn');
const registerBtn = document.getElementById('register-btn');
const logoutBtn = document.getElementById('logout-btn');
const startBtn = document.getElementById('start-round-btn');
const submitBtn = document.getElementById('submit-answer-btn');
const radiusSlider = document.getElementById('radius-slider');
let sliderRadius = parseFloat(radiusSlider.value);  // Current value of slider

// ========== Init Game Space ==========

function ensureUserId() {
  let userId = localStorage.getItem('userId');
  
  if (!userId) {
    userId = 'guest-' + crypto.randomUUID();
    localStorage.setItem('userId', userId);
    localStorage.setItem('role', "GUEST");
    console.log("[Frontend][Init] Generate guest userId: ", userId);
  }
}

function updateAuthUI() {
  const isGuest = localStorage.getItem('role') === 'GUEST';

  loginBtn.style.display = isGuest ? 'inline-block' : 'none';
  registerBtn.style.display = isGuest ? 'inline-block' : 'none';
  logoutBtn.style.display = isGuest ? 'none' : 'inline-block';
  
  // radiusSlider.disable = roundStarted ? true : false;
  document.getElementById('radius-ui').style.display = isGuest || roundStarted ? 'none' : 'block';
  startBtn.disabled = isGuest || roundStarted;
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
  } else {
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

    data.landmarks.forEach(lm => {
      const polygon = L.polygon(lm.coordinates, {
        color: "grey",
        fillOpacity: 0.3
      }).addTo(INIT_MAP);

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
    localStorage.setItem('userId', userInfo.userId);
    localStorage.setItem('username', userInfo.username);
    localStorage.setItem('role', userInfo.role);

    alert('Logged In.');
    updateAuthUI();
    initMap();
    setupInteractions();
    initGame();
    drawRadiusCircle(); 
  })
}

function logout(){
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.setItem('role', "GUEST");
  resetGameToInit(); 
  initMap();
  updateAuthUI(); 
  setupInteractions();
  alert('Logut successful.')
}

// ========== Player Movement ==========

function fetchPlayerCoord() {
  // reference 
  // Real time location tracker app on leafletjs || HTML5 geolocation || Tekson
  // https://www.youtube.com/watch?v=8KX4_4NK7ZY

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

      updatePlayerViewCone();
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
        updatePlayerViewCone();
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

// ======== Game Mechanism ========

function startRound() {
  let role = localStorage.getItem('role');
  if (role === 'GUEST') {
    console.log("[Frontend] Please Log in First");
    return;
  }

  if (!playerCoord) {
    alert("Waiting for location... Please allow GPS or wait a few seconds.");
    return;
  }

  roundStarted = true;
  radiusSlider.disabled = true;

  if (searchCircle){
    INIT_MAP.removeLayer(searchCircle);
    searchCircle = null;
  }

  const requestBody = {
    userId: localStorage.getItem('userId'),
    latitude: playerCoord?.lat ?? 0.0,
    longitude: playerCoord?.lng ?? 0.0,
    angle: playerAngle ?? 0.0,
    radiusMeters: sliderRadius
  };

  fetch(LOCAL_HOST + '/api/game/start-round', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestBody)
  })
  .then(res => {
    if (!res.ok) throw new Error('[Frontend] Failed to fetch current target');
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
  })
  .catch(err => {
    console.error("[Frontend] startRound error:", err);
    alert("Failed to start round.");
    drawRadiusCircle();
  });

  // startBtn.disabled = true;
  startBtn.style.display = 'inline-block';
  startBtn.innerText = "Finish Round";
  startBtn.onclick = finishRound; 
  submitBtn.disabled = false;
  submitBtn.style.display = 'inline-block';
}

function submitAnswer() {
  const secondsUsed = stopCountdown();
  if (secondsUsed == null) {
    alert("⏱ Time not running!");
    return;
  }

  const requestBody = {
    userId: localStorage.getItem('userId'),
    secondsUsed: secondsUsed
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

    // ===== ipdate target info =====
    if (data.target) {
      const newName = data.target.name;
      const attemptsLeft = data.target.attemptsLeft;
      const riddle = data.target.riddle;

      document.getElementById('target-info').innerText = newName;
      document.getElementById('chances-left').style.display = 'block';
      document.getElementById('chances-left').innerText = `Remaining Attempts: ${attemptsLeft}`;
      document.getElementById('riddle-box').innerText = riddle ? riddle : "(No riddle)";

      // restarting countdown
      const oldName = document.getElementById('target-info').innerText;
      if (data.isCorrect || oldName !== newName) {
        setTimeout(() => {
          startCountdown();
        }, 100);  // ensuring UI not stuck
      }
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

  document.getElementById('countdown-timer').textContent = "";
  document.getElementById('countdown-timer').style.display = 'none';
  document.getElementById('target-info').innerText = "(No target yet)";
  document.getElementById('chances-left').style.display = 'none';
  document.getElementById('riddle-box').innerText = "";

  submitBtn.disabled = true;
  startBtn.disabled = false;
  startBtn.innerText = "Start Round";
  startBtn.onclick = startRound;

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

function updatePlayerViewCone() {

  if (!playerCoord || (playerCoord.lat == null || playerCoord.lng == null)) return;

  let angle = (localStorage.getItem('role') === 'ADMIN') ? testPlayerAngle : playerAngle;
  if (angle == null) return;

  const resolution = 20;  
  const startAngle = angle - spanDeg / 2;
  const endAngle = angle + spanDeg / 2;

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
}

function calculateAngle(start, end) {
  const dy = end.lat - start.lat;
  const dx = end.lng - start.lng;
  const theta = Math.atan2(dx, dy);
  let angle = theta * (180 / Math.PI);
  if (angle < 0) angle += 360;
  return angle;
}

function initOrientationListener() {
  if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
    // iOS 
    DeviceOrientationEvent.requestPermission()
      .then(permissionState => {
        if (permissionState === 'granted') {
          window.addEventListener('deviceorientation', handleOrientation, true);
        }
      })
      .catch(console.error);
  } else {
    // Android 
    window.addEventListener('deviceorientation', handleOrientation, true);
  }
}

function handleOrientation(event) {
  
  let heading;

  if (event.webkitCompassHeading !== undefined) {
    heading = event.webkitCompassHeading;
  } else if (event.alpha !== null) {
    heading = 360 - event.alpha;
    console.warn("[Frontend][Orientation] Fallback to alpha. May be imprecise.");
  }

  if (heading !== undefined) {
    playerAngle = heading;

    if (playerCoord && playerCoord.lat != null) { 
      updatePlayerViewCone();
      if (playerMarker) playerMarker.setRotationAngle(playerAngle);
    }

    document.getElementById('angle-display').innerText = `Angle: ${heading.toFixed(2)}°`;
  }
}


// ========== Main ==========

document.addEventListener('DOMContentLoaded', () => {
  ensureUserId();
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

  document.querySelector('.info-box').insertAdjacentHTML('beforeend', '<div id="angle-display">Angle: ?</div>');
  
})