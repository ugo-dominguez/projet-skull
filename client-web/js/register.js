const form = document.getElementById('register-form');
const message = document.getElementById('message');
const submitBtn = document.getElementById('submit-btn');

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const email = document.getElementById('email').value.trim();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;
  const confirm = document.getElementById('confirm').value;

  if (password !== confirm) {
    showMessage(message, 'Les mots de passe ne correspondent pas.');
    return;
  }

  submitBtn.disabled = true;
  try {
    // POST /players -> { id, email, username }
    await apiRequest(CONFIG.PLAYER_SERVICE_URL, '/players', {
      method: 'POST',
      body: JSON.stringify({ email, username, password }),
    });
    showMessage(message, 'Compte créé ! Redirection vers la connexion...', 'success');
    setTimeout(() => location.href = 'login.html', 1200);
  } catch (err) {
    showMessage(message, err.status ? err.message : 'Impossible de joindre le serveur.');
    submitBtn.disabled = false;
  }
});
