package fr.miage.skull.ladderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_records")
@Getter @Setter @NoArgsConstructor
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", nullable = false, unique = true)
    private UUID gameId;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    @OneToMany(mappedBy = "gameRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameRecordPlayer> players = new ArrayList<>();

    public void addPlayer(GameRecordPlayer p) {
        p.setGameRecord(this);
        players.add(p);
    }
}