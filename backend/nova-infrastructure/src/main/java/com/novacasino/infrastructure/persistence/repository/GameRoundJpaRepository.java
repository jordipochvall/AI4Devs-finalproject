package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.projection.TopGameProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Spring Data repository for the auditable game-round log (append-only). Audit search uses
 * {@link JpaSpecificationExecutor} so only the filters actually present are added to the SQL (avoids
 * null-typed parameters); ordering/paging come from the {@code Pageable} (the service defaults to
 * {@code created_at DESC}, served by the {@code idx_game_rounds_*_created} indexes — readme §3.2.8).
 */
public interface GameRoundJpaRepository extends JpaRepository<GameRoundEntity, Long>,
        JpaSpecificationExecutor<GameRoundEntity> {

    /** Free-spin child rounds of a triggering round, in chronological order (for replay). */
    java.util.List<GameRoundEntity> findByTriggeringRoundIdOrderByIdAsc(Long triggeringRoundId);

    /** A player's own rounds, newest first (HU-14). */
    Page<GameRoundEntity> findByPlayerIdOrderByCreatedAtDesc(Long playerId, Pageable pageable);

    /** An operator's rounds in a window, in chain order (by id) — for integrity verification (HU-20). */
    List<GameRoundEntity> findByOperatorIdAndCreatedAtBetweenOrderByIdAsc(
            Long operatorId, OffsetDateTime from, OffsetDateTime to);

    // --- HU-16 dashboard aggregations (operator-scoped, date-bounded) ---

    /** Distinct players with at least one round in the window. */
    @Query("""
            SELECT COUNT(DISTINCT r.playerId) FROM GameRoundEntity r
            WHERE r.operatorId = :operatorId AND r.createdAt BETWEEN :from AND :to""")
    long countActivePlayers(@Param("operatorId") Long operatorId,
                            @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /** Total wagered in cents in the window (RFJ report, HU-21). */
    @Query("""
            SELECT COALESCE(SUM(r.betCents), 0) FROM GameRoundEntity r
            WHERE r.operatorId = :operatorId AND r.createdAt BETWEEN :from AND :to""")
    long sumBetCents(@Param("operatorId") Long operatorId,
                     @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /** Total paid out in cents in the window (RFJ report, HU-21). */
    @Query("""
            SELECT COALESCE(SUM(r.winCents), 0) FROM GameRoundEntity r
            WHERE r.operatorId = :operatorId AND r.createdAt BETWEEN :from AND :to""")
    long sumWinCents(@Param("operatorId") Long operatorId,
                     @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /** Gross gaming revenue in cents: total wagered minus total paid out. */
    @Query("""
            SELECT COALESCE(SUM(r.betCents), 0) - COALESCE(SUM(r.winCents), 0) FROM GameRoundEntity r
            WHERE r.operatorId = :operatorId AND r.createdAt BETWEEN :from AND :to""")
    long ggrCents(@Param("operatorId") Long operatorId,
                  @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /** Total rounds (base + free spins) in the window. */
    @Query("""
            SELECT COUNT(r) FROM GameRoundEntity r
            WHERE r.operatorId = :operatorId AND r.createdAt BETWEEN :from AND :to""")
    long countRounds(@Param("operatorId") Long operatorId,
                     @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /** Net loss in cents (wagered − won) of a player since an instant — for loss-limit checks (HU-19). */
    @Query("""
            SELECT COALESCE(SUM(r.betCents), 0) - COALESCE(SUM(r.winCents), 0) FROM GameRoundEntity r
            WHERE r.playerId = :playerId AND r.createdAt >= :since""")
    long netLossSince(@Param("playerId") Long playerId, @Param("since") OffsetDateTime since);

    /** Most-played games (by round count) in the window, ordered desc; cap via {@code Pageable}. */
    @Query("""
            SELECT r.gameId AS gameId, COUNT(r) AS rounds FROM GameRoundEntity r
            WHERE r.operatorId = :operatorId AND r.createdAt BETWEEN :from AND :to
            GROUP BY r.gameId ORDER BY COUNT(r) DESC""")
    List<TopGameProjection> topGames(@Param("operatorId") Long operatorId,
                                     @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
                                     Pageable pageable);
}
