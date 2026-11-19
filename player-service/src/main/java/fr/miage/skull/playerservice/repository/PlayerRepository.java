package fr.miage.skull.playerservice.repository;

import fr.miage.skull.playerservice.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByEmail(String email);
    boolean existsByEmail(String email);
}