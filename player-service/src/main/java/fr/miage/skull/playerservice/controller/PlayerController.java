package fr.miage.skull.playerservice.controller;

import fr.miage.skull.playerservice.model.Player;
import fr.miage.skull.playerservice.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/players")
public class PlayerController {

    record CreatePlayer(String email, String username, String password) {}
    record LoginRequest(String email, String password) {}
    record PlayerView(UUID id, String email, String username, boolean admin, boolean banned) {}
    record LoginResponse(UUID id, String email, String username, boolean admin, String token) {}

    private final PlayerRepository repo;
    private final PasswordEncoder encoder;

    public PlayerController(PlayerRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerView create(@RequestBody CreatePlayer req) {
        if (req.email() == null || req.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requis");
        }
        if (req.username() == null || req.username().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username requis");
        }
        if (req.password() == null || req.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe trop court (min 6)");
        }
        if (repo.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }
        Player p = new Player();
        p.setEmail(req.email());
        p.setUsername(req.username());
        p.setPassword(encoder.encode(req.password()));
        p.setAdmin(false);
        p.setBanned(false);
        p = repo.save(p);
        return toView(p);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        Player p = repo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        if (!encoder.matches(req.password(), p.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        if (p.isBanned()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte banni");
        }
        String token = "skull-" + UUID.randomUUID();
        return new LoginResponse(p.getId(), p.getEmail(), p.getUsername(), p.isAdmin(), token);
    }

    @GetMapping
    public List<PlayerView> list() {
        return repo.findAll().stream().map(PlayerController::toView).toList();
    }

    private static PlayerView toView(Player p) {
        return new PlayerView(p.getId(), p.getEmail(), p.getUsername(), p.isAdmin(), p.isBanned());
    }
}
