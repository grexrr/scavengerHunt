document.addEventListener('DOMContentLoaded', () => {
  const map = L.map('map').setView([51.8940, -8.4902], 17);   //cork
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(map);

  let gameInitialized = false;
  let playerMarker = null;
  let searchRadiusCircle = null;

  const radiusSlider = document.getElementById('radius-slider');
  const radiusValueDisplay = document.getElementById('radius-value');
  const startButton = document.getElementById('start-round-btn');

  // Radius Slider
  radiusSlider.addEventListener('input', () => {
    const radius = parseInt(radiusSlider.value);
    radiusValueDisplay.textContent = radius;
    if (searchRadiusCircle && playerMarker) {
      searchRadiusCircle.setRadius(radius);
    }
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
      console.log('[FrontEnd] Current target:', data);
      const targetDiv = document.getElementById('target-info');
      targetDiv.innerHTML = `
        <b>${data.name}</b><br/>
        Riddle: ${data.riddle}<br/>
        Lat: ${data.latitude.toFixed(6)}, Lng: ${data.longitude.toFixed(6)}
      `;

      L.marker([data.latitude, data.longitude], {
        title: 'Target: ' + data.name
      }).addTo(map);

    })
    .catch(err => {
      console.error('[FrontEnd] Round Start Failed:', err);
    });    
  });


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
  }
});
