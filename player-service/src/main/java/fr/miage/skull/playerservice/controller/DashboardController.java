package fr.miage.skull.playerservice.controller;

import fr.miage.skull.playerservice.model.Player;
import fr.miage.skull.playerservice.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/players/{id}/dashboard")
public class DashboardController {

    public record DashboardView(PlayerInfo player, Object openGames, Object history) {}
    public record PlayerInfo(UUID id, String email, String username, boolean admin) {}

    private final PlayerRepository players;
    private final RestTemplate rest;
    private final String gameUrl;
    private final String ladderUrl;

    public DashboardController(PlayerRepository players, RestTemplate rest,
                               @Value("${services.game.url}") String gameUrl,
                               @Value("${services.ladder.url}") String ladderUrl) {
        this.players = players;
        this.rest = rest;
        this.gameUrl = gameUrl;
        this.ladderUrl = ladderUrl;
    }

    @GetMapping
    public DashboardView get(@PathVariable UUID id) {
        Player p = players.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur inconnu"));

        PlayerInfo info = new PlayerInfo(p.getId(), p.getEmail(), p.getUsername(), p.isAdmin());

        // Appel game-service pour les parties ouvertes
        Object openGames;
        try {
            openGames = rest.getForObject(gameUrl + "/games", List.class);
        } catch (Exception e) {
            openGames = "Service indisponible (" + e.getMessage() + ")";
        }

        // Appel ladder-service pour l'historique
        Object history;
        try {
            history = rest.getForObject(ladderUrl + "/users/" + id + "/history", List.class);
        } catch (Exception e) {
            history = "Service indisponible (" + e.getMessage() + ")";
        }

        return new DashboardView(info, openGames, history);
    }
}
