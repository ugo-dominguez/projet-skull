package fr.miage.skull.gameservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "player_games")
@Getter @Setter @NoArgsConstructor
public class PlayerGame {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false)
    private int seat;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(name = "is_ai", nullable = false)
    private boolean ai;

    @Column(name = "skin_id")
    private UUID skinId;

    @Column(nullable = false)
    private int score;

    @Column(name = "remaining_discs", nullable = false)
    private int remainingDiscs = 4;

    @Column(name = "is_eliminated", nullable = false)
    private boolean eliminated;

    @Column(name = "has_left", nullable = false)
    private boolean hasLeft;

    @OneToMany(mappedBy = "playerGame", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disc> discs = new ArrayList<>();

    public boolean controlledByAi() {
        return ai || hasLeft;
    }
}