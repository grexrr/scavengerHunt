// ========== Global Variables ==========
let dragStart = null;  // Stores the starting point of mouse drag
let playerMarker = null;  // The player's arrow marker on the INIT_MAP
let geoWatchId = null;

const icon = L.icon({     // Custom icon for player marker
  iconUrl: 'arrow.png',
  iconSize: [32, 32],
  iconAnchor: [16, 16]
});

let searchCircle = null;  // Circle representing search radius
let userId = null;
let roundStarted = false;

let testPlayerAngle = null;   // Test Player facing angle

let playerCoord = null;    // Normal Player Coordination;
let currentTargetCoord = null;


// const LOCAL_HOST = "http://localhost:8080";   // Backend base URL
const LOCAL_HOST = "https://e36427c21dff.ngrok-free.app "  // Ngrok
const ADMIN_TEST_COORD = L.latLng(51.8940, -8.4902);
const INIT_MAP = L.map('map');  // Initialize INIT_MAP centered at UCC for test admin

// ========== DOM Elements ==========
const loginBtn = document.getElementById('login-btn');
const registerBtn = document.getElementById('register-btn');
const logoutBtn = document.getElementById('logout-btn');
const startBtn = document.getElementById('start-round-btn');
const submitBtn = document.getElementById('submit-answer-btn');
const radiusSlider = document.getElementById('radius-slider');
let currentRadius = parseFloat(radiusSlider.value);  // Current value of slider

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

function initMap() {
  const userRole = localStorage.getItem('role');
  updateAuthUI();
  
  if(userRole === "ADMIN"){
    INIT_MAP.setView(ADMIN_TEST_COORD, 17);
    console.log('[Frontend][Init] Admin map initialized at UCC');
    //update testPlayer Position
    playerMarker = L.marker(ADMIN_TEST_COORD, {icon}).addTo(INIT_MAP);
  } else {
    // RESET GUEST/PLAYER COORD
    fetchPlayerCoord();
  }
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
  })
}

function logout(){
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.setItem('role', "GUEST");
  initMap();
  updateAuthUI(); 
  alert('Logut successful.')
}

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


// ======== Submit Answer ========

// ========== Movement Functions ==========

function fetchPlayerCoord() {

  if (geoWatchId !== null) {
    navigator.geolocation.clearWatch(geoWatchId);
    geoWatchId = null;
    console.log('[Frontend] Stopped previous geolocation tracking');
  }

  if (!navigator.geolocation){
    alert('Geolocation unavailable.');
    return;
  } 
  geoWatchId = navigator.geolocation.watchPosition(
    position => {
      let lat = position.coords.latitude;
      let lng = position.coords.longitude;
      playerCoord = L.latLng(lat, lng);

      
      
      if(!playerMarker) {  
        INIT_MAP.setView(playerCoord, 17);
        playerMarker = L.marker(playerCoord, {icon}).addTo(INIT_MAP);
      } else {
        playerMarker.setLatLng(playerCoord);
        INIT_MAP.setView(playerCoord, 17);
      }
    },
    err => {
      console.error("[Geolocation error]", err);
      alert('Failed to get your location. Please check your permissions.');
    },
    { 
      enableHighAccuracy: true,
      timeout: 10000,           // 10 seconds
      maximumAge: 5000          // 5 seconds
    }
  );
}

function trackPlayerMovement() {
}

// ========== Tool Functions ==========

function updateAuthUI() {
  if (localStorage.getItem('role') === 'GUEST') {
    loginBtn.style.display = "inline-block";
    registerBtn.style.display = 'inline-block';
    logoutBtn.style.display = 'none';
    startBtn.disabled = true;
  } else {
    loginBtn.style.display = "none";
    registerBtn.style.display = 'none';
    logoutBtn.style.display = 'inline-block';
    startBtn.disabled = false;
  }
}

function calculateAngle(start, end) {
  const dy = end.lat - start.lat;
  const dx = end.lng - start.lng;
  const theta = Math.atan2(dx, dy);
  let angle = theta * (180 / Math.PI);
  if (angle < 0) angle += 360;
  return angle;
}

// ========== Main ==========

document.addEventListener('DOMContentLoaded', () => {
  ensureUserId();
  
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(INIT_MAP);
  
  initMap();
})