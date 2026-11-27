package fr.miage.skull.playerservice.config;

import fr.miage.skull.playerservice.model.Player;
import fr.miage.skull.playerservice.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private static final String ADMIN_EMAIL = "admin@skull.fr";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final PlayerRepository repo;
    private final PasswordEncoder encoder;

    public AdminInitializer(PlayerRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.existsByEmail(ADMIN_EMAIL)) {
            log.info("Admin déjà présent ({}), skip.", ADMIN_EMAIL);
            return;
        }
        Player admin = new Player();
        admin.setEmail(ADMIN_EMAIL);
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(encoder.encode(ADMIN_PASSWORD));
        admin.setAdmin(true);
        admin.setBanned(false);
        repo.save(admin);
        log.info("Compte admin créé : {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
