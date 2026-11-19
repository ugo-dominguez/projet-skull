package fr.miage.skull.gameservice.controller;

import fr.miage.skull.gameservice.service.LobbyService;
import fr.miage.skull.gameservice.service.LobbyService.GameView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {

    record CreateGame(int maxPlayers, int aiCount, UUID skinId) {}
    record JoinGame(UUID skinId) {}

    private final LobbyService lobby;

    public GameController(LobbyService lobby) {
        this.lobby = lobby;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameView create(@RequestHeader("X-User-Id") UUID userId,
                           @RequestHeader("X-Username") String username,
                           @RequestBody CreateGame req) {
        return lobby.create(userId, username, req.maxPlayers(), req.aiCount(), req.skinId());
    }

    @GetMapping
    public List<GameView> listOpen() {
        return lobby.listOpen();
    }

    @GetMapping("/{id}")
    public GameView get(@PathVariable UUID id) {
        return lobby.get(id);
    }

    @PostMapping("/{id}/join")
    public GameView join(@PathVariable UUID id,
                         @RequestHeader("X-User-Id") UUID userId,
                         @RequestHeader("X-Username") String username,
                         @RequestBody(required = false) JoinGame req) {
        return lobby.join(id, userId, username, req == null ? null : req.skinId());
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable UUID id, @RequestHeader("X-User-Id") UUID userId) {
        lobby.leave(id, userId);
    }
}