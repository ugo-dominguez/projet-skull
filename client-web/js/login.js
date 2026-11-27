const form = document.getElementById('login-form');
const message = document.getElementById('message');
const submitBtn = document.getElementById('submit-btn');

if (getToken()) location.replace('dashboard.html');

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  submitBtn.disabled = true;
  try {
    // ATTENTION : cet endpoint n'existe pas encore dans player-service.
    // Contrat attendu : { "token": "<JWT>", "id": "<uuid>", "username": "<pseudo>" }
    const data = await apiRequest(CONFIG.PLAYER_SERVICE_URL, '/players/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    saveSession(data.token, { id: data.id, username: data.username });
    location.href = 'dashboard.html';
  } catch (err) {
    showMessage(message, err.status === 401 ? 'Identifiants incorrects ou compte banni.' : (err.status ? err.message : 'Impossible de joindre le serveur.'));
    submitBtn.disabled = false;
  }
});
