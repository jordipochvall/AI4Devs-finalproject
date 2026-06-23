package com.novacasino.application.audit;

import com.novacasino.common.dto.IntegrityReportDto;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Tamper-evident verification of the {@code game_rounds} integrity chain (HU-20). Pure application
 * use case: depends only on the {@link AuditChainPort} (not on JPA or Spring). Recomputes each round's
 * hash exactly as the DB trigger does ({@code prev | id | player | game | bet | win | balancePost |
 * rngSeed}, SHA-256 hex) and reports the first round whose stored hash diverges.
 */
public class VerifyIntegrityUseCase {

    private static final OffsetDateTime MIN_INSTANT = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final AuditChainPort auditChain;

    public VerifyIntegrityUseCase(final AuditChainPort auditChain) {
        this.auditChain = auditChain;
    }

    /** Verifies the operator's chain over [from, to] (null bounds default to a wide window). */
    @Transactional
    public IntegrityReportDto verify(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        final OffsetDateTime effFrom = from != null ? from : MIN_INSTANT;
        final OffsetDateTime effTo   = to   != null ? to   : OffsetDateTime.now();

        final List<AuditRound> rounds = auditChain.roundsInWindow(operatorId, effFrom, effTo);
        if (rounds.isEmpty()) {
            return new IntegrityReportDto(effFrom, effTo, 0, true, null);
        }

        String prev = rounds.get(0).prevHash() != null ? rounds.get(0).prevHash() : "";
        long checked = 0;
        for (final AuditRound r : rounds) {
            checked++;
            final String expected = sha256hex(canonical(prev, r));
            if (!expected.equals(r.rowHash())) {
                return new IntegrityReportDto(effFrom, effTo, checked, false, r.id());
            }
            prev = r.rowHash(); // advance with the stored link (pinpoints a single tamper)
        }
        return new IntegrityReportDto(effFrom, effTo, checked, true, null);
    }

    /** The exact preimage the DB trigger hashes for a round. */
    private String canonical(final String prev, final AuditRound r) {
        return prev + "|" + r.id() + "|" + r.playerId() + "|" + r.gameId() + "|"
                + r.betCents() + "|" + r.winCents() + "|" + r.balancePostCents() + "|" + r.rngSeed();
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
