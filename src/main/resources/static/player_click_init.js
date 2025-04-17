function attachInitHandler(map, onInitialized) {
    map.on('click', function (e) {
      const { lat, lng } = e.latlng;
      const angle = 0;
  
      console.log('[FrontEnd] Initializing...');
      fetch('http://localhost:8080/api/game/init', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ latitude: lat, longitude: lng, angle: angle })
      })
      .then(res => res.text())
      .then(msg => {
        console.log('[FrontEnd] Initialized Success:', msg);
        if (onInitialized) onInitialized(lat, lng);
      })
      .catch(err => {
        console.error('[FrontEnd] Initialized Fail:', err);
      });
    });
  }
  