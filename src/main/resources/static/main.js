const map = L.map('map').setView([51.8940, -8.4902], 17); // Cork
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '© OpenStreetMap'
}).addTo(map);

let gameInitialized = false;

// player_click_init.js 
attachInitHandler(map, (lat, lng) => {
  gameInitialized = true;
  console.log(`[FrontEnd] Player initialized at (${lat}, ${lng})`);
});
