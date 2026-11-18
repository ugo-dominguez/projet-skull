package fr.miage.skull.marketservice.repository;

import fr.miage.skull.marketservice.model.UserSkin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkinRepository extends JpaRepository<UserSkin, UUID> {
    List<UserSkin> findByUserId(UUID userId);
    Optional<UserSkin> findByUserIdAndSkinId(UUID userId, UUID skinId);
    Optional<UserSkin> findByUserIdAndEquippedTrue(UUID userId);
}