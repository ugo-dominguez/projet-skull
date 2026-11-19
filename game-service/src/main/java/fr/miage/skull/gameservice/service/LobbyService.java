package fr.miage.skull.gameservice.service;

import fr.miage.skull.gameservice.model.Game;
import fr.miage.skull.gameservice.model.GameStatus;
import fr.miage.skull.gameservice.model.PlayerGame;
import fr.miage.skull.gameservice.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class LobbyService {

    public static final int MIN_PLAYERS = 3;
    public static final int MAX_PLAYERS = 6;

    public record PlayerView(String username, boolean ai, int seat, boolean left) {}
    public record GameView(UUID id, GameStatus status, int maxPlayers, int aiCount,
                           int humanSlots, int humansJoined, UUID creatorId,
                           List<PlayerView> players) {}

    private final GameRepository games;
    private final GameEngine engine;

    public LobbyService(GameRepository games, GameEngine engine) {
        this.games = games;
        this.engine = engine;
    }

    @Transactional
    public GameView create(UUID userId, String username, int maxPlayers, int aiCount, UUID skinId) {
        if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "maxPlayers doit être entre " + MIN_PLAYERS + " et " + MAX_PLAYERS);
        }
        if (aiCount < 0 || aiCount > maxPlayers - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "aiCount doit être entre 0 et " + (maxPlayers - 1));
        }

        Game g = new Game();
        g.setMaxPlayers(maxPlayers);
        g.setAiCount(aiCount);
        g.setCreatorId(userId);

        for (int i = 0; i < aiCount; i++) {
            addPlayer(g, null, "IA-" + (i + 1), true, null);
        }
        addPlayer(g, userId, username, false, skinId);
        startIfFull(g);
        return toView(games.save(g));
    }

    @Transactional(readOnly = true)
    public List<GameView> listOpen() {
        return games.findByStatusOrderByCreatedAtDesc(GameStatus.WAITING).stream()
                .map(LobbyService::toView).toList();
    }

    @Transactional(readOnly = true)
    public GameView get(UUID gameId) {
        return toView(games.findById(gameId).orElseThrow(LobbyService::notFound));
    }

    @Transactional
    public GameView join(UUID gameId, UUID userId, String username, UUID skinId) {
        Game g = games.findByIdForUpdate(gameId).orElseThrow(LobbyService::notFound);

        if (g.getStatus() != GameStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partie a déjà commencé");
        }
        if (g.getPlayerGames().stream().anyMatch(p -> userId.equals(p.getUserId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà dans cette partie");
        }
        if (g.humanCount() >= g.humanSlots()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Partie complète");
        }

        addPlayer(g, userId, username, false, skinId);
        startIfFull(g);
        return toView(g);
    }

    @Transactional
    public void leave(UUID gameId, UUID userId) {
        Game g = games.findByIdForUpdate(gameId).orElseThrow(LobbyService::notFound);

        PlayerGame me = g.getPlayerGames().stream()
                .filter(p -> userId.equals(p.getUserId()) && !p.isHasLeft())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'êtes pas dans cette partie"));

        switch (g.getStatus()) {
            case WAITING -> {
                g.getPlayerGames().remove(me);
                for (int i = 0; i < g.getPlayerGames().size(); i++) {
                    g.getPlayerGames().get(i).setSeat(i);
                }
                if (g.humanCount() == 0) {
                    games.delete(g);
                } else if (userId.equals(g.getCreatorId())) {
                    g.getPlayerGames().stream().filter(p -> !p.isAi()).findFirst()
                            .ifPresent(p -> g.setCreatorId(p.getUserId()));
                }
            }
            case IN_PROGRESS -> {
                me.setHasLeft(true);
                if (g.activeHumanCount() == 0) {
                    g.setStatus(GameStatus.FINISHED);
                }
            }
            case FINISHED -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Partie terminée");
        }
    }

    private void startIfFull(Game g) {
        if (g.humanCount() == g.humanSlots()) {
            engine.start(g);
        }
    }

    private static void addPlayer(Game g, UUID userId, String username, boolean ai, UUID skinId) {
        PlayerGame pg = new PlayerGame();
        pg.setGame(g);
        pg.setSeat(g.getPlayerGames().size());
        pg.setUserId(userId);
        pg.setUsername(username);
        pg.setAi(ai);
        pg.setSkinId(skinId);
        g.getPlayerGames().add(pg);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Partie inconnue");
    }

    private static GameView toView(Game g) {
        return new GameView(g.getId(), g.getStatus(), g.getMaxPlayers(), g.getAiCount(),
                g.humanSlots(), g.humanCount(), g.getCreatorId(),
                g.getPlayerGames().stream()
                        .map(p -> new PlayerView(p.getUsername(), p.controlledByAi(), p.getSeat(), p.isHasLeft()))
                        .toList());
    }
}