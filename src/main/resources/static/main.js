let gameInitialized = false;
let playerMarker = null;
let searchRadiusCircle = null;
let currentTargetLatLng = null;

const radiusSlider = document.getElementById('radius-slider');
const radiusValueDisplay = document.getElementById('radius-value');
const startButton = document.getElementById('start-round-btn');
const targetDiv = document.getElementById('target-info');

// targetIcon
const redIcon = L.icon({
  iconUrl: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
  iconSize: [25, 41], 
  iconAnchor: [12, 41],
});




document.addEventListener('DOMContentLoaded', () => {
  const map = L.map('map').setView([51.8940, -8.4902], 17);   //cork
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(map);
  //coordinate reference system


  // Radius Slider
  radiusSlider.addEventListener('input', () => {
    const radius = parseInt(radiusSlider.value);
    radiusValueDisplay.textContent = radius;
    if (searchRadiusCircle && playerMarker) {
      searchRadiusCircle.setRadius(radius);
    }
  });

  // Submit Button
  const submitButton = document.getElementById('submit-answer-btn');
  submitButton.addEventListener('click', () => {
    submitAnswer();
  });

  // Click to init player
  attachInitHandler(map, (lat, lng) => {
    gameInitialized = true;
    console.log(`[FrontEnd] Player initialized at (${lat}, ${lng})`);
    document.getElementById('radius-ui').style.display = 'block';
    updatePlayerPositionOnMap(lat, lng);
  });

  // startRound Request
  startButton.addEventListener('click', () => {
    const radius = parseInt(radiusSlider.value);

    if (!playerMarker) {
      alert("Player not set.");
      return;
    }

    const { lat, lng } = playerMarker.getLatLng();
    const angle = 0;

    const payload = {
      latitude: lat,
      longitude: lng,
      angle: angle,
      radius: radius
    };

    console.log('[FrontEnd] Sending start round:', payload);

    fetch(`http://localhost:8080/api/game/start-round`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    .then(res => res.text())
    .then(msg => {
      console.log('[FrontEnd] Round Started:', msg);
      alert("New round started.");
      return fetch('http://localhost:8080/api/game/target');
    })
    .then(res => {
      if( !res.ok ) {
        throw new Error('Target not available');  
      }
      return res.json();
    })
    .then(data => {
      
      if (searchRadiusCircle) {
        map.removeLayer(searchRadiusCircle);
        searchRadiusCircle = null;
      }

      console.log('[FrontEnd] Current target:', data);
      const targetDiv = document.getElementById('target-info');
      targetDiv.innerHTML = `
        <b>${data.name}</b><br/>
        Riddle: ${data.riddle}<br/>
        Lat: ${data.latitude.toFixed(6)}, Lng: ${data.longitude.toFixed(6)}
      `;

      currentTargetLatLng = L.latLng(data.latitude, data.longitude);
      // future switch
      L.marker([data.latitude, data.longitude], {
        icon: redIcon,
        title: 'Target: ' + data.name
      }).addTo(map);
    })
    .catch(err => {
      console.error('[FrontEnd] Round Start Failed:', err);
    });    
  });

  window.updatePlayerPositionOnMap = updatePlayerPositionOnMap;


  // Display Player & Radius
  function updatePlayerPositionOnMap(lat, lng) {
    const radius = parseInt(radiusSlider.value);

    if (!playerMarker) {
      playerMarker = L.marker([lat, lng], { title: 'Player' }).addTo(map);
    } else {
      playerMarker.setLatLng([lat, lng]);
    }

    if (!searchRadiusCircle) {
      searchRadiusCircle = L.circle([lat, lng], {
        radius: radius,
        color: 'blue',
        fillColor: '#cce5ff',
        fillOpacity: 0.3
      }).addTo(map);
    } else {
      searchRadiusCircle.setLatLng([lat, lng]);
      searchRadiusCircle.setRadius(radius);
    }

    document.getElementById('coord').innerText = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
    checkProximityToTarget(lat, lng);

    const angle = 0;
    fetch('http://localhost:8080/api/game/update-position', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ latitude: lat, longitude: lng, angle: angle })
    })
    .then(res => res.text())
    .then(msg => {
      console.log('[FrontEnd] Player position synced:', msg);
    })
    .catch(err => {
      console.error('[FrontEnd] Player position sync failed:', err);
    });
  }

  function checkProximityToTarget(lat, lng) {
    if (!currentTargetLatLng) return;

    const playerLatLng = L.latLng(lat, lng);
    const distance = playerLatLng.distanceTo(currentTargetLatLng); // unit: m

    console.log('[FrontEnd] Distance to target:', distance.toFixed(2), 'm');

    if (distance < 50) {
      if (!window.answerPromptShown) {
        window.answerPromptShown = true;
        submitButton.style.display = 'inline-block'; 
      }
    } else {
      window.answerPromptShown = false;
      submitButton.style.display = 'none'; 
    }
  }


  function submitAnswer() {
    fetch('http://localhost:8080/api/game/submit-answer', {
      method: 'POST'
    })
    .then(res => res.text())
    .then(msg => {
      console.log('[FrontEnd] Answer submitted:', msg);
  
      if (msg.includes("All riddles solved")) {
        alert(" You've completed all the puzzles!");
        document.getElementById('submit-answer-btn').style.display = 'none';
        document.getElementById('target-info').innerHTML = "(Game finished!)";
        location.reload();
        return;
      }
  
      // fetch next target
      return fetch('http://localhost:8080/api/game/target');
    })
    .then(res => {
      if (!res || !res.ok) return;
  
      return res.json();
    })
    .then(data => {
      if (!data) return;
  
      console.log('[FrontEnd] New target:', data);
  
      // update info box
      
      targetDiv.innerHTML = `
        <b>${data.name}</b><br/>
        Riddle: ${data.riddle}<br/>
        Lat: ${data.latitude.toFixed(6)}, Lng: ${data.longitude.toFixed(6)}
      `;
  
      // update target marker
      currentTargetLatLng = L.latLng(data.latitude, data.longitude);
      L.marker([data.latitude, data.longitude], {
        icon: redIcon,
        title: 'Target: ' + data.name
      }).addTo(map);
  
      window.answerPromptShown = false;
    })
    .catch(err => {
      console.error('[FrontEnd] Submit failed:', err);
    });
  }

  function attachInitHandler(map, onInitialized) {
    map.on('click', function (e) {
      const { lat, lng } = e.latlng;
      const angle = 0;
  
      if (!window.gameInitialized) {
        // first init
        fetch('http://localhost:8080/api/game/init', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ latitude: lat, longitude: lng, angle: angle })
        })
        .then(res => res.text())
        .then(msg => {
          console.log('[FrontEnd] Initialized Success:', msg);
          window.gameInitialized = true;
  
          document.getElementById('radius-ui').style.display = 'block';
          if (onInitialized) onInitialized(lat, lng);
        })
        .catch(err => {
          console.error('[FrontEnd] Initialized Fail:', err);
        });
      } else {
        // update init
        updatePlayerPositionOnMap(lat, lng);
      }
    });
  }
});

