package fr.miage.skull.ladderservice.repository;

import fr.miage.skull.ladderservice.model.PlayerStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PlayerStatsRepository extends JpaRepository<PlayerStats, UUID> {

    @Query("select s from PlayerStats s order by s.wins desc, s.gamesPlayed asc")
    List<PlayerStats> rankByWins(Pageable pageable);

    @Query("""
           select s from PlayerStats s where s.gamesPlayed > 0
           order by (1.0 * s.wins / s.gamesPlayed) desc, s.wins desc
           """)
    List<PlayerStats> rankByRatio(Pageable pageable);
}