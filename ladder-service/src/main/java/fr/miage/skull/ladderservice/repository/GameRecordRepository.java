package fr.miage.skull.ladderservice.repository;

import fr.miage.skull.ladderservice.model.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRecordRepository extends JpaRepository<GameRecord, UUID> {
    boolean existsByGameId(UUID gameId);

    @Query("""
           select distinct r from GameRecord r join fetch r.players
           where r.gameId = :gameId
           """)
    Optional<GameRecord> findDetailByGameId(@Param("gameId") UUID gameId);

    @Query("""
           select distinct r from GameRecord r join fetch r.players
           where r.id in (select p.gameRecord.id from GameRecordPlayer p where p.userId = :userId)
           order by r.finishedAt desc
           """)
    List<GameRecord> findHistoryOfUser(@Param("userId") UUID userId);
}