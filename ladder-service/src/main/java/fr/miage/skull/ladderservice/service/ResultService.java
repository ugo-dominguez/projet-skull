package fr.miage.skull.ladderservice.service;

import fr.miage.skull.ladderservice.model.GameRecord;
import fr.miage.skull.ladderservice.model.GameRecordPlayer;
import fr.miage.skull.ladderservice.model.PlayerStats;
import fr.miage.skull.ladderservice.repository.GameRecordRepository;
import fr.miage.skull.ladderservice.repository.PlayerStatsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ResultService {

    public record PlayerResult(UUID userId, String displayName, boolean ai,
                               int score, int remainingDiscs,
                               boolean winner, boolean hasLeft) {}

    public record GameResult(UUID gameId, Instant finishedAt, List<PlayerResult> players) {}

    private final GameRecordRepository records;
    private final PlayerStatsRepository stats;

    public ResultService(GameRecordRepository records, PlayerStatsRepository stats) {
        this.records = records;
        this.stats = stats;
    }

    @Transactional
    public GameRecord record(GameResult result) {
        if (result.players() == null || result.players().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun joueur dans la partie");
        }
        if (records.existsByGameId(result.gameId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Partie déjà enregistrée");
        }

        GameRecord rec = new GameRecord();
        rec.setGameId(result.gameId());
        rec.setFinishedAt(result.finishedAt() != null ? result.finishedAt() : Instant.now());

        for (PlayerResult pr : result.players()) {
            GameRecordPlayer p = new GameRecordPlayer();
            p.setUserId(pr.ai() ? null : pr.userId());
            p.setDisplayName(pr.displayName());
            p.setAi(pr.ai());
            p.setScore(pr.score());
            p.setRemainingDiscs(pr.remainingDiscs());
            p.setWinner(pr.winner());
            p.setHasLeft(pr.hasLeft());
            rec.addPlayer(p);

            if (!pr.ai() && pr.userId() != null) {
                PlayerStats s = stats.findById(pr.userId()).orElseGet(() -> {
                    PlayerStats n = new PlayerStats();
                    n.setUserId(pr.userId());
                    return n;
                });
                s.setUsername(pr.displayName());
                s.setGamesPlayed(s.getGamesPlayed() + 1);
                if (pr.winner()) s.setWins(s.getWins() + 1);
                stats.save(s);
            }
        }
        return records.save(rec);
    }
}