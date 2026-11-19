package fr.miage.skull.marketservice.repository;

import fr.miage.skull.marketservice.model.Skin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SkinRepository extends JpaRepository<Skin, UUID> {
    boolean existsByName(String name);
}