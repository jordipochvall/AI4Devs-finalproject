package com.novacasino.api.operator;

import com.novacasino.api.operator.dto.IntegrityReportDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Tamper-evident verification of the {@code game_rounds} integrity chain (HU-20). Recomputes each
 * round's hash exactly as the DB trigger does ({@code prev | id | player_id | game_id | bet | win |
 * balance_post | rng_seed}, SHA-256 hex) and reports the first round whose stored hash diverges.
 */
@Service
public class IntegrityService {

    private static final OffsetDateTime MIN_INSTANT = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final GameRoundJpaRepository roundRepo;

    public IntegrityService(final GameRoundJpaRepository roundRepo) {
        this.roundRepo = roundRepo;
    }

    /** Verifies the operator's chain over [from, to] (null bounds default to a wide window). */
    @Transactional(readOnly = true)
    public IntegrityReportDto verify(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        final OffsetDateTime effFrom = from != null ? from : MIN_INSTANT;
        final OffsetDateTime effTo   = to   != null ? to   : OffsetDateTime.now();

        final List<GameRoundEntity> rounds =
                roundRepo.findByOperatorIdAndCreatedAtBetweenOrderByIdAsc(operatorId, effFrom, effTo);
        if (rounds.isEmpty()) {
            return new IntegrityReportDto(effFrom, effTo, 0, true, null);
        }

        // Seed with the boundary link stored on the first row (genesis is the empty string).
        String prev = rounds.get(0).getPrevHash() != null ? rounds.get(0).getPrevHash() : "";
        long checked = 0;
        for (final GameRoundEntity r : rounds) {
            checked++;
            final String expected = sha256hex(canonical(prev, r));
            if (!expected.equals(r.getRowHash())) {
                return new IntegrityReportDto(effFrom, effTo, checked, false, r.getId());
            }
            prev = r.getRowHash(); // advance with the stored link (pinpoints a single tamper)
        }
        return new IntegrityReportDto(effFrom, effTo, checked, true, null);
    }

    /** The exact preimage the DB trigger hashes for a round. */
    private String canonical(final String prev, final GameRoundEntity r) {
        return prev + "|" + r.getId() + "|" + r.getPlayerId() + "|" + r.getGameId() + "|"
                + r.getBetCents() + "|" + r.getWinCents() + "|" + r.getBalancePostCents() + "|" + r.getRngSeed();
    }

    private static String sha256hex(final String input) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(64);
            for (final byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
