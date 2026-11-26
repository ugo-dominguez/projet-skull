const TOKEN_KEY = 'skull_token';
const USER_KEY = 'skull_user'; // { id, username }

function getToken() { return localStorage.getItem(TOKEN_KEY); }
function getUser() {
  try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; }
}

function saveSession(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// Appel HTTP générique.
// - ajoute le JWT (Authorization) si présent
// - ajoute X-User-Id / X-Username, attendus par game-service
async function apiRequest(baseUrl, path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const token = getToken();
  const user = getUser();
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (user) {
    headers['X-User-Id'] = user.id;
    headers['X-Username'] = user.username;
  }

  const res = await fetch(baseUrl + path, { ...options, headers });
  const text = await res.text();

  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }

  if (!res.ok) {
    const message = (data && data.message) || (typeof data === 'string' && data) || `Erreur ${res.status}`;
    const error = new Error(message);
    error.status = res.status;
    throw error;
  }
  return data;
}

function showMessage(element, text, type = 'error') {
  element.textContent = text;
  element.className = `message ${type}`;
  element.hidden = false;
}
