package fr.miage.skull.playerservice.controller;

import fr.miage.skull.playerservice.model.Player;
import fr.miage.skull.playerservice.repository.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/players")
public class PlayerController {
    record CreatePlayer(String email, String username, String password) {}
    record PlayerView(UUID id, String email, String username) {}

    private final PlayerRepository repo;
    private final PasswordEncoder encoder;

    public PlayerController(PlayerRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @PostMapping
    public PlayerView create(@RequestBody CreatePlayer req) {
        Player p = new Player();
        p.setEmail(req.email());
        p.setUsername(req.username());
        p.setPassword(encoder.encode(req.password()));
        p = repo.save(p);
        return new PlayerView(p.getId(), p.getEmail(), p.getUsername());
    }

    @GetMapping
    public List<PlayerView> list() {
        return repo.findAll().stream()
                .map(p -> new PlayerView(p.getId(), p.getEmail(), p.getUsername()))
                .toList();
    }
}