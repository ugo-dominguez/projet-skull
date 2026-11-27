package fr.miage.skull.playerservice.controller;

import fr.miage.skull.playerservice.model.Player;
import fr.miage.skull.playerservice.model.Report;
import fr.miage.skull.playerservice.model.ReportStatus;
import fr.miage.skull.playerservice.repository.PlayerRepository;
import fr.miage.skull.playerservice.repository.ReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class ReportController {

    record CreateReport(UUID reporterId, UUID reportedId, UUID gameId, String reason) {}
    record ReportView(UUID id, UUID reporterId, String reporterName,
                      UUID reportedId, String reportedName,
                      UUID gameId, String reason, Instant date, ReportStatus status) {}

    private final ReportRepository reports;
    private final PlayerRepository players;

    public ReportController(ReportRepository reports, PlayerRepository players) {
        this.reports = reports;
        this.players = players;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportView create(@RequestBody CreateReport req) {
        if (req.reason() == null || req.reason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Raison requise");
        }
        Player reporter = players.findById(req.reporterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Signaleur inconnu"));
        Player reported = players.findById(req.reportedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur signalé inconnu"));

        Report r = new Report();
        r.setReporter(reporter);
        r.setReported(reported);
        r.setGameId(req.gameId());
        r.setReason(req.reason());
        r.setReportDate(Instant.now());
        r.setStatus(ReportStatus.PENDING);
        reports.save(r);
        return toView(r);
    }

    @GetMapping
    public List<ReportView> listPending() {
        return reports.findByStatusOrderByReportDateDesc(ReportStatus.PENDING)
                .stream().map(ReportController::toView).toList();
    }

    private static ReportView toView(Report r) {
        return new ReportView(r.getId(), r.getReporter().getId(), r.getReporter().getUsername(),
                r.getReported().getId(), r.getReported().getUsername(),
                r.getGameId(), r.getReason(), r.getReportDate(), r.getStatus());
    }
}
