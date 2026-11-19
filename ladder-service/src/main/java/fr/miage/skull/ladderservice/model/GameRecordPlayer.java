package fr.miage.skull.ladderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "game_record_players")
@Getter @Setter @NoArgsConstructor
public class GameRecordPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_record_id")
    private GameRecord gameRecord;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "is_ai", nullable = false)
    private boolean ai;

    @Column(nullable = false)
    private int score;

    @Column(name = "remaining_discs", nullable = false)
    private int remainingDiscs;

    @Column(nullable = false)
    private boolean winner;

    @Column(name = "has_left", nullable = false)
    private boolean hasLeft;
}