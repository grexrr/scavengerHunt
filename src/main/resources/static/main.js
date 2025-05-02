let playerMarker = null;
let searchCircle = null;

let roundStarted = false;
let playerLat = null;
let playerLng = null;
let playerAngle = null;

const localhost = "http://localhost:8080";
const map = L.map('map').setView([51.8940, -8.4902], 17);

const loginBtn = document.getElementById('login-btn');
const registerBtn = document.getElementById('register-btn');
const logoutBtn = document.getElementById('logout-btn');
const startBtn = document.getElementById('start-round-btn');

const radiusSlider = document.getElementById('radius-slider');
let currentRadius = parseFloat(radiusSlider.value);

const playerId = localStorage.getItem('playerId');


//functions

function ensurePlayerId() {
  let playerId = localStorage.getItem('playerId');
  if (!playerId) {
    playerId = 'guest-' + crypto.randomUUID(); 
    localStorage.setItem('playerId', playerId);
    console.log('[Init] Generated guest playerId:', playerId);
  }
  return playerId;
}

function updateAuthUI() {
  if (playerId) {
    loginBtn.style.display = 'none';
    registerBtn.style.display = 'none';   
    logoutBtn.style.display = 'inline-block';
  } else {
    loginBtn.style.display = 'inline-block';
    registerBtn.style.display = 'inline-block'; 
    logoutBtn.style.display = 'none';
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
  .then(playerId => {
    //localStorage for MVP
    localStorage.setItem('playerId', playerId);
    updateAuthUI();
    alert('Login successful!');
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
  localStorage.removeItem('playerId');
  updateAuthUI();
  alert('Logged out.');
  location.reload();
}

function updatePlayerPosition(lat, lng, angle){
  const playerId = localStorage.getItem('playerId');
  fetch(localhost + "/api/game/update-position", {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      playerId: playerId,
      latitude: lat,
      longitude: lng,
      angle: angle
    })
  })
  .then(res => res.text())
  .then(msg => {
    console.log("[FrontEnd] Position updated", msg);
    if (!playerMarker) {
      playerMarker = L.marker([lat, lng], { title: 'Player' }).addTo(map);
    } else {
      playerMarker.setLatLng([lat, lng])  
    }
  })
}

function searchRadius(radiusMeter){
  const playerId = localStorage.getItem('playerId');

  if (!playerMarker) {
    alert("Player not initialized on map.");
    return;
  }

  fetch(localhost + '/api/game/start-round', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      playerId: playerId,
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
  })
  .catch(err => {
    console.log('[Frontend] Round started Failure: ', err);
  });
}

//main

document.addEventListener('DOMContentLoaded', () => {
  const playerId = ensurePlayerId();
  
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

  map.on('click', function (e) {
    playerLat = e.latlng.lat;
    playerLng = e.latlng.lng;
    playerAngle = 0; 
    updatePlayerPosition(playerLat, playerLng, playerAngle);

    if (!roundStarted && localStorage.getItem('playerId')) {
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
      }
    }
  });

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
  })

  startBtn.addEventListener('click', () => {
    
    searchRadius(currentRadius);
    if (searchCircle) {
      map.removeLayer(searchCircle);
      searchCircle = null;
    }
    document.getElementById('radius-ui').style.display = 'none';
    roundStarted = true;
  })
})


