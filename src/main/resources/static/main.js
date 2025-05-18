// ========== Global Variables ==========
let dragStart = null;  // Stores the starting point of mouse drag
let playerMarker = null;  // The player's arrow marker on the map
const icon = L.icon({     // Custom icon for player marker
  iconUrl: 'arrow.png',
  iconSize: [32, 32],
  iconAnchor: [16, 16]
});

let searchCircle = null;  // Circle representing search radius
let userId = null;
let roundStarted = false;
let playerLat = null;     // Player latitude
let playerLng = null;     // Player longitude
let playerAngle = null;   // Player facing angle
let currentTargetLat = null;
let currentTargetLng = null;

const localhost = "http://localhost:8080"; // Backend base URL
const map = L.map('map').setView([51.8940, -8.4902], 17);  // Initialize map centered at UCC

// ========== DOM Elements ==========
const loginBtn = document.getElementById('login-btn');
const registerBtn = document.getElementById('register-btn');
const logoutBtn = document.getElementById('logout-btn');
const startBtn = document.getElementById('start-round-btn');
const submitBtn = document.getElementById('submit-answer-btn');
const radiusSlider = document.getElementById('radius-slider');
let currentRadius = parseFloat(radiusSlider.value);  // Current value of slider

// ========== Authentication ==========
function getUserId() {
  return localStorage.getItem('userId');
}

function ensurePlayerId() {
  let userId = getUserId();
  if (!userId) {
    userId = 'guest-' + crypto.randomUUID(); 
    localStorage.setItem('userId', userId);
    console.log('[Init] Generated guest userId:', userId);
  }
  return userId;
}

function updateAuthUI() {
  if (getUserId() && !getUserId().startsWith('guest')) {
    loginBtn.style.display = 'none';
    registerBtn.style.display = 'none';   
    logoutBtn.style.display = 'inline-block';
    startBtn.disabled = false;
  } else {
    loginBtn.style.display = 'inline-block';
    registerBtn.style.display = 'inline-block'; 
    logoutBtn.style.display = 'none';
    startBtn.disabled = true;
  }
}

function login(username, password) {
  fetch(localhost + '/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  .then(res => {
    if (!res.ok) throw new Error('Invalid username or password.');
    return res.text();
  })
  .then(userId => {
    localStorage.setItem('userId', userId);
    updateAuthUI();
    alert('Login successful!');
    location.reload();
  })
  .catch(err => {
    console.error('Login failed:', err);
    alert('Login failed.');
  });
}

function register(username, password) {
  fetch(localhost + '/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({username, password})
  })
  .then(res => {
    if (res.status === 409) {
      alert('Username already exists.');
      throw new Error('Username already exists.');
    }
    return res.text();
  })
  .then(msg => {
    console.log('Register successful: ', msg);
    login(username, password)
  })
  .catch(err => {
    console.error('Register failed: ', err);
    alert('Register failed')
  })
}

function logout(){
  localStorage.removeItem('userId');
  updateAuthUI();
  alert('Logged out.');
  location.reload();
}

// ========== API Call: Update Position ==========
function updatePlayerPosition(lat, lng, angle) {
  const userId = getUserId();
  return fetch(localhost + "/api/game/update-position", {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, latitude: lat, longitude: lng, angle })
  })
  .then(res => res.text())
  .then(msg => {
    console.log("[Frontend] Position updated", msg);
  });
}

// ========== Math: Angle Calculation ==========
function calculateAngle(start, end) {
  const dy = end.lat - start.lat;
  const dx = end.lng - start.lng;
  const theta = Math.atan2(dx, dy);
  let angle = theta * (180 / Math.PI);
  if (angle < 0) angle += 360;
  return angle;
}

// ========== Math: Proximity and Angle ==========
function checkProximityAndDirection() {
  if (currentTargetLat == null || playerLat == null) return;

  const playerPoint = L.latLng(playerLat, playerLng);
  const targetPoint = L.latLng(currentTargetLat, currentTargetLng);
  const distance = playerPoint.distanceTo(targetPoint); // meter

  const angleToTarget = calculateAngle(
    { lat: playerLat, lng: playerLng },
    { lat: currentTargetLat, lng: currentTargetLng }
  );

  const angleDiff = Math.abs(playerAngle - angleToTarget);
  const adjustedDiff = angleDiff > 180 ? 360 - angleDiff : angleDiff;

  console.log(`[Frontend] Distance to target: ${distance.toFixed(2)}m`);
  console.log(`[Frontend] Facing: ${playerAngle.toFixed(2)}°, Target Angle: ${angleToTarget.toFixed(2)}°, Diff: ${adjustedDiff.toFixed(2)}°`);

  if (distance < 50 && adjustedDiff <= 30) {
    document.getElementById('submit-answer-btn').style.display = 'inline-block';
  } else {
    document.getElementById('submit-answer-btn').style.display = 'none';
  }
}

// ========== API Call: Start Round ==========
function searchRadius(radiusMeter){
  let userId = getUserId();

  if (!playerMarker) {
    alert("Player not initialized on map.");
    return;
  } else {
    const latlng = playerMarker.getLatLng();
    playerLat = latlng.lat;
    playerLng = latlng.lng;
  }

  fetch(localhost + '/api/game/start-round', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: userId,
      latitude: playerLat,
      longitude: playerLng,
      radiusMeters: radiusMeter  
    })
  }) 
  .then(res => res.text())
  .then(msg => {
    console.log('[Frontend] Round started: ', msg);
    roundStarted = true;
    alert('New Round Started');
    if (searchCircle) {
      map.removeLayer(searchCircle);
      searchCircle = null;
    }
  })
  .then(()=>{
    selectFirstTarget();
  })
  .catch(err => {
    console.log('[Frontend] Round started Failure: ', err);
  });
}

// ========== API Call: Get Target ==========
function selectFirstTarget(){
  let userId = getUserId();

  fetch(localhost + "/api/game/first-target?userId=" + userId)
  .then(res => {
    if(!res.ok){
      throw new Error("No target available.")
    }
    return res.json();
  })
  .then(target => {
    console.log("[Frontend] Current target: ", target);
    document.getElementById('target-info').innerText = target.name + "\n" + target.riddle;
    currentTargetLat = target.latitude;
    currentTargetLng = target.longitude;
  })
  .catch(err => {
    console.log("[Frontend] No target found:", err);
    document.getElementById('target-info').innerText = "(No target yet)";
  });
}

// ========== API Call: Submit Current Landmark ==========

function submitCurrentLandmark() {
  return fetch(localhost + '/api/game/submit-answer', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: getUserId() })
  })
  .then(async res => {
    const contentType = res.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      return res.json(); // next landmark
    } else {
      return res.text(); // "All riddles solved!" or error
    }
  });
}


// ========== Main ==========
document.addEventListener('DOMContentLoaded', () => {
  const userId = ensurePlayerId();
  document.getElementById('radius-ui').style.display = 'none';

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(map);

  updateAuthUI();

  registerBtn.addEventListener('click', () => {
    const username = prompt('Enter username:');
    const password = prompt('Enter password:');
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

  // ======== Map Interaction: Player Drag & Rotate ========
  map.on('mousedown', function(e) {
    dragStart = e.latlng;
    map.dragging.disable();

    if (!playerMarker) {
      playerMarker = L.marker(dragStart, {
        icon: icon,
        rotationAngle: 0,
        rotationOrigin: 'center center'
      }).addTo(map);
    } else {
      playerMarker.setLatLng(dragStart);
      playerMarker.setRotationAngle(0);
    }
  });

  map.on('mousemove', function(e) {
    if (!dragStart || !playerMarker) return;
    const angle = calculateAngle(dragStart, e.latlng);
    playerMarker.setRotationAngle(angle);
  });

  map.on('mouseup', function(e) {
    if (!dragStart || !playerMarker) return;

    const angle = calculateAngle(dragStart, e.latlng);
    const lat = dragStart.lat;
    const lng = dragStart.lng;

    playerLat = lat;
    playerLng = lng;
    playerAngle = angle;

    updatePlayerPosition(lat, lng, angle).then(() => {
      checkProximityAndDirection();  
    });

    dragStart = null;
    map.dragging.enable();

    if (!roundStarted && getUserId() && !getUserId().startsWith('guest')) {
      document.getElementById('radius-ui').style.display = 'block';

      if (!searchCircle) {
        searchCircle = L.circle([playerLat, playerLng], {
          radius: currentRadius,
          color: 'blue',
          fillColor: '#cce5ff',
          fillOpacity: 0.3
        }).addTo(map);
      } else {
        searchCircle.setLatLng([playerLat, playerLng]);
        searchCircle.setRadius(currentRadius);
      }
    }
  });

  // ======== UI Slider: Radius Control ========
  radiusSlider.addEventListener('input', () => {
    currentRadius = parseFloat(radiusSlider.value);

    if (!searchCircle) {
      searchCircle = L.circle([playerLat, playerLng], {
        radius: currentRadius,
        color: 'blue',
        fillColor: '#cce5ff',
        fillOpacity: 0.3
      }).addTo(map);
    } else {
      searchCircle.setLatLng([playerLat, playerLng]);
      searchCircle.setRadius(currentRadius);
    }

    checkProximityAndDirection();
  });

  // ======== Start Round ========
  startBtn.addEventListener('click', () => {
    if (playerLat == null || playerLng == null) {
      alert("Please click on the map to set your position first.");
      return;
    }

    updatePlayerPosition(playerLat, playerLng, playerAngle)
      .then(() => {
        searchRadius(currentRadius);
      })
      .catch(err => {
        console.error('Failed to update position before starting round', err);
      });
  });

  // ======== Submit Answer ========

  submitBtn.addEventListener('click', () => {
    submitCurrentLandmark()
      .then(data => {
        if (typeof data === "string") {
          alert(data);
          document.getElementById('target-info').innerText = data;
          submitBtn.style.display = 'none';
        } else {
          currentTargetLat = data.latitude;
          currentTargetLng = data.longitude;
          document.getElementById('target-info').innerText = data.name + "\n" + data.riddle;
          checkProximityAndDirection(); 
        }
      })
      .catch(err => {
        alert("❌ Submission failed");
        console.error("Submit error:", err);
      });
  });  
});