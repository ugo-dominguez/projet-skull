package fr.miage.skull.ladderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "player_stats")
@Getter @Setter @NoArgsConstructor
public class PlayerStats {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed;

    @Column(nullable = false)
    private int wins;

    public double ratio() {
        return gamesPlayed == 0 ? 0.0 : (double) wins / gamesPlayed;
    }
}