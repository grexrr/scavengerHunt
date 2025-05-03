let playerMarker = null;
let searchCircle = null;
let userId = null;

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

// const userId = localStorage.getItem('userId');


//functions
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
    //localStorage for MVP
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

function updatePlayerPosition(lat, lng, angle){
  let userId = getUserId();
  return fetch(localhost + "/api/game/update-position", {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      userId: userId,
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
  });
}


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
  })
  .catch(err => {
    console.log('[Frontend] Round started Failure: ', err);
  });
}

function selectedNextTarget(){
  let userId = getUserId();

  fetch(localhost + "/api/game/next-target?userId=" + userId)
  .then(res => {
    if(!res.ok){
      throw new Error("No target available.")
    }
    return res.json();
  })
  .then(target => {
    console.log("[Frontend] Current target: ", target);
    document.getElementById('target-info').innerText = target.name + "\n" + target.riddle;
  })
  .catch(err => {
    console.log("[Frontend] No target found:", err);
    document.getElementById('target-info').innerText = "(No target yet)";
});
}


//main

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

  map.on('click', function (e) {
    playerLat = e.latlng.lat;
    playerLng = e.latlng.lng;
    playerAngle = 0; 
    updatePlayerPosition(playerLat, playerLng, playerAngle);

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

})


