const backend = "http://localhost:8080";
const map = L.map('map').setView([51.8940, -8.4902], 17);

const loginBtn = document.getElementById('login-btn');
const registerBtn = document.getElementById('register-btn');
const logoutBtn = document.getElementById('logout-btn');

//functions
function setPlayerCurrentPosition(){}

function updateAuthUI() {
  const playerId = localStorage.getItem('playerId');
  const loginBtn = document.getElementById('login-btn');
  const logoutBtn = document.getElementById('logout-btn');
  const registerBtn = document.getElementById('register-btn'); 

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
  fetch(backend + '/api/auth/login', {
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
  fetch(backend + '/api/auth/register', {
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


document.addEventListener('DOMContentLoaded', () => {
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

})