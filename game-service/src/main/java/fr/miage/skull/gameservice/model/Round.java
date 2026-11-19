package fr.miage.skull.gameservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rounds")
@Getter @Setter @NoArgsConstructor
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_status", nullable = false)
    private RoundStatus status = RoundStatus.PLACING;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("bidOrder asc")
    private List<Bid> bids = new ArrayList<>();
}