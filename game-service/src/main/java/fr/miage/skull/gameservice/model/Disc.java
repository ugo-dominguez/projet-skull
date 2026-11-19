package fr.miage.skull.gameservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "discs")
@Getter @Setter @NoArgsConstructor
public class Disc {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_game_id")
    private PlayerGame playerGame;

    @Enumerated(EnumType.STRING)
    @Column(name = "disc_type", nullable = false)
    private DiscType type;

    @Column(name = "pile_position")
    private Integer pilePosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscStatus status = DiscStatus.IN_HAND;
}