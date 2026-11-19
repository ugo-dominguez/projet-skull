package fr.miage.skull.gameservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "bids")
@Getter @Setter @NoArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_game_id")
    private PlayerGame playerGame;

    @Column(name = "bid_value", nullable = false)
    private int value;

    @Column(name = "bid_order", nullable = false)
    private int bidOrder;
}