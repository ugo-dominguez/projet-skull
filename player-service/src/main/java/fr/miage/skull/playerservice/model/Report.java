package fr.miage.skull.playerservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Player reporter;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id")
    private Player reported;

    @Column(name = "game_id")
    private UUID gameId;

    @Column(nullable = false)
    private String reason;

    @Column(name = "report_date", nullable = false)
    private Instant reportDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;
}