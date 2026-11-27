package fr.miage.skull.playerservice.repository;

import fr.miage.skull.playerservice.model.Report;
import fr.miage.skull.playerservice.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByStatusOrderByReportDateDesc(ReportStatus status);
}
