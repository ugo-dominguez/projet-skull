if (!getToken() || !getUser()) location.replace('login.html');

const MIN_PLAYERS = 3;
const MAX_PLAYERS = 6;

const listEl = document.getElementById('game-list');
const emptyEl = document.getElementById('empty');
const message = document.getElementById('message');
const createForm = document.getElementById('create-form');

document.getElementById('logout-btn').addEventListener('click', logout);
document.getElementById('refresh-btn').addEventListener('click', loadGames);
createForm.addEventListener('submit', createGame);

function logout() {
  clearSession();
  location.replace('login.html');
}

function handleAuthError(err) {
  if (err.status === 401 || err.status === 403) {
    logout();
    return true;
  }
  return false;
}

// GET /games renvoie toutes les parties : on ne garde que celles en attente de joueurs
function isOpen(game) {
  return game.status === 'WAITING' && game.humansJoined < game.humanSlots;
}

function isInGame(game, username) {
  return game.players.some((p) => !p.ai && p.username === username && !p.left);
}

async function loadGames() {
  message.hidden = true;
  try {
    const games = await apiRequest(CONFIG.GAME_SERVICE_URL, '/games');
    renderGames(games);
  } catch (err) {
    if (!handleAuthError(err)) {
      showMessage(message, err.status ? err.message : 'Impossible de charger les parties.');
    }
  }
}

function renderGames(games) {
  const me = getUser();
  const visible = games.filter((g) => isOpen(g) || isInGame(g, me.username));

  listEl.innerHTML = '';
  emptyEl.hidden = visible.length > 0;

  visible.forEach((game) => {
    const li = document.createElement('li');
    li.className = 'game-item';

    const info = document.createElement('div');
    const title = document.createElement('strong');
    title.textContent = `Partie ${game.id.slice(0, 8)}`;
    const meta = document.createElement('div');
    meta.className = 'game-meta';
    meta.textContent = `${game.humansJoined}/${game.humanSlots} joueurs humains · ${game.aiCount} IA · ${game.status}`;
    info.append(title, meta);

    const button = document.createElement('button');
    button.className = 'small';
    if (isInGame(game, me.username)) {
      button.textContent = 'Quitter';
      button.classList.add('secondary');
      button.addEventListener('click', () => leaveGame(game.id, button));
    } else {
      button.textContent = 'Rejoindre';
      button.addEventListener('click', () => joinGame(game.id, button));
    }

    li.append(info, button);
    listEl.appendChild(li);
  });
}

async function createGame(event) {
  event.preventDefault();
  const maxPlayers = parseInt(document.getElementById('max-players').value, 10);
  const aiCount = parseInt(document.getElementById('ai-count').value, 10);

  if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
    showMessage(message, `Le nombre de joueurs doit être entre ${MIN_PLAYERS} et ${MAX_PLAYERS}.`);
    return;
  }
  if (aiCount < 0 || aiCount >= maxPlayers) {
    showMessage(message, 'Le nombre d\'IA doit être positif et inférieur au nombre de joueurs.');
    return;
  }

  try {
    // POST /games  body : { maxPlayers, aiCount, skinId }  (skinId: null = apparence par défaut)
    await apiRequest(CONFIG.GAME_SERVICE_URL, '/games', {
      method: 'POST',
      body: JSON.stringify({ maxPlayers, aiCount, skinId: null }),
    });
    showMessage(message, 'Partie créée.', 'success');
    loadGames();
  } catch (err) {
    if (!handleAuthError(err)) {
      showMessage(message, err.status ? err.message : 'Impossible de créer la partie.');
    }
  }
}

async function joinGame(gameId, button) {
  button.disabled = true;
  try {
    await apiRequest(CONFIG.GAME_SERVICE_URL, `/games/${gameId}/join`, { method: 'POST', body: '{}' });
    showMessage(message, 'Partie rejointe.', 'success');
    loadGames();
  } catch (err) {
    if (!handleAuthError(err)) {
      showMessage(message, err.status ? err.message : 'Impossible de rejoindre la partie.');
      button.disabled = false;
    }
  }
}

async function leaveGame(gameId, button) {
  button.disabled = true;
  try {
    // POST /games/{id}/leave -> 204 No Content
    await apiRequest(CONFIG.GAME_SERVICE_URL, `/games/${gameId}/leave`, { method: 'POST' });
    showMessage(message, 'Vous avez quitté la partie.', 'success');
    loadGames();
  } catch (err) {
    if (!handleAuthError(err)) {
      showMessage(message, err.status ? err.message : 'Impossible de quitter la partie.');
      button.disabled = false;
    }
  }
}

loadGames();
