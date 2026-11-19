package fr.miage.skull.ladderservice.controller;

import fr.miage.skull.ladderservice.model.GameRecord;
import fr.miage.skull.ladderservice.model.PlayerStats;
import fr.miage.skull.ladderservice.repository.GameRecordRepository;
import fr.miage.skull.ladderservice.repository.PlayerStatsRepository;
import fr.miage.skull.ladderservice.service.ResultService;
import fr.miage.skull.ladderservice.service.ResultService.GameResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
public class LadderController {

    record PlayerLine(UUID userId, String displayName, boolean ai, int score,
                      int remainingDiscs, boolean winner, boolean hasLeft) {}
    record GameView(UUID gameId, Instant finishedAt, List<PlayerLine> players) {}
    record RankingEntry(int rank, UUID userId, String username,
                        int gamesPlayed, int wins, double ratio) {}
    record StatsView(UUID userId, String username, int gamesPlayed, int wins, double ratio) {}

    private final ResultService resultService;
    private final GameRecordRepository records;
    private final PlayerStatsRepository stats;

    public LadderController(ResultService resultService, GameRecordRepository records,
                            PlayerStatsRepository stats) {
        this.resultService = resultService;
        this.records = records;
        this.stats = stats;
    }

    private static GameView toView(GameRecord r) {
        return new GameView(r.getGameId(), r.getFinishedAt(),
                r.getPlayers().stream()
                        .map(p -> new PlayerLine(p.getUserId(), p.getDisplayName(), p.isAi(),
                                p.getScore(), p.getRemainingDiscs(), p.isWinner(), p.isHasLeft()))
                        .toList());
    }

    @PostMapping("/games/results")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public GameView record(@RequestBody GameResult result) {
        return toView(resultService.record(result));
    }

    @GetMapping("/users/{userId}/history")
    @Transactional(readOnly = true)
    public List<GameView> history(@PathVariable UUID userId) {
        return records.findHistoryOfUser(userId).stream().map(LadderController::toView).toList();
    }

    @GetMapping("/games/{gameId}/result")
    @Transactional(readOnly = true)
    public GameView detail(@PathVariable UUID gameId) {
        return records.findDetailByGameId(gameId).map(LadderController::toView)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partie inconnue"));
    }

    @GetMapping("/users/{userId}/stats")
    public StatsView userStats(@PathVariable UUID userId) {
        PlayerStats s = stats.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune partie jouée"));
        return new StatsView(s.getUserId(), s.getUsername(), s.getGamesPlayed(), s.getWins(), s.ratio());
    }

    @GetMapping("/ladder")
    public List<RankingEntry> ranking(@RequestParam(defaultValue = "wins") String sort,
                                      @RequestParam(defaultValue = "10") int limit) {
        var page = PageRequest.of(0, Math.min(Math.max(limit, 1), 100));
        List<PlayerStats> list = switch (sort) {
            case "wins" -> stats.rankByWins(page);
            case "ratio" -> stats.rankByRatio(page);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort doit valoir wins ou ratio");
        };
        return IntStream.range(0, list.size())
                .mapToObj(i -> {
                    PlayerStats s = list.get(i);
                    return new RankingEntry(i + 1, s.getUserId(), s.getUsername(),
                            s.getGamesPlayed(), s.getWins(), s.ratio());
                }).toList();
    }
}