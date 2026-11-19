package fr.miage.skull.gameservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter @Setter @NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.WAITING;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @Column(name = "ai_count", nullable = false)
    private int aiCount;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seat asc")
    private List<PlayerGame> playerGames = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber asc")
    private List<Round> rounds = new ArrayList<>();

    public int humanSlots() {
        return maxPlayers - aiCount;
    }

    public int humanCount() {
        return (int) playerGames.stream().filter(p -> !p.isAi()).count();
    }

    public int activeHumanCount() {
        return (int) playerGames.stream().filter(p -> !p.isAi() && !p.isHasLeft()).count();
    }
}