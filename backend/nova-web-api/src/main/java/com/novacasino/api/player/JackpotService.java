package com.novacasino.api.player;

import com.novacasino.infrastructure.persistence.entity.JackpotGrantEntity;
import com.novacasino.infrastructure.persistence.entity.JackpotPoolEntity;
import com.novacasino.infrastructure.persistence.repository.JackpotGrantJpaRepository;
import com.novacasino.infrastructure.persistence.repository.JackpotPoolJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Progressive jackpot (HU-26): each bet contributes a configured fraction to the per-game pool and the
 * grant decision is <b>deterministic</b> in the round seed — the same seed and odds always yield the
 * same answer, so production, simulator and replay agree (AC2). Integer arithmetic throughout.
 * Persistence runs inside the caller's spin transaction; the pool uses optimistic locking (@Version).
 */
@Service
public class JackpotService {

    private static final Logger log = LoggerFactory.getLogger(JackpotService.class);

    /** Mixed into the seed so the jackpot draw is independent of the reel RNG stream. */
    private static final long SEED_SALT = 0x9E3779B97F4A7C15L;
    private static final int BPS_DENOMINATOR = 10_000;

    private final JackpotPoolJpaRepository poolRepo;
    private final JackpotGrantJpaRepository grantRepo;

    public JackpotService(final JackpotPoolJpaRepository poolRepo, final JackpotGrantJpaRepository grantRepo) {
        this.poolRepo = poolRepo;
        this.grantRepo = grantRepo;
    }

    /** The configured pool for a game, if the game has a jackpot. */
    public Optional<JackpotPoolEntity> findPool(final Long gameId) {
        return poolRepo.findByGameId(gameId);
    }

    /** Integer contribution of a bet to the pool (bet × bps / 10000). */
    public long contribution(final JackpotPoolEntity pool, final long betCents) {
        return betCents * pool.getContributionBps() / BPS_DENOMINATOR;
    }

    /** Deterministic grant decision: same seed + odds → same result (AC2). 1-in-N probability. */
    public static boolean isAwarded(final long seed, final long oddsDenominator) {
        final long draw = new Random(seed ^ SEED_SALT).nextLong();
        return Math.floorMod(draw, oddsDenominator) == 0L;
    }

    /** Applies the outcome to the pool: reset to seed if awarded, otherwise add the contribution. */
    public void applyOutcome(final JackpotPoolEntity pool, final boolean awarded, final long contribution) {
        pool.setCurrentCents(awarded ? pool.getSeedCents() : pool.getCurrentCents() + contribution);
        pool.setUpdatedAt(OffsetDateTime.now());
        poolRepo.save(pool);
    }

    /** Records an immutable jackpot grant tied to the winning round. */
    public void recordGrant(final Long operatorId, final Long gameId, final Long roundId,
                            final Long playerId, final long amountCents) {
        grantRepo.save(new JackpotGrantEntity(operatorId, gameId, roundId, playerId, amountCents));
        log.info("Jackpot awarded: gameId={}, round={}, userId={}, amount={} cents",
                gameId, roundId, playerId, amountCents);
    }
}
