package com.novacasino.application.audit;

/**
 * Application-level view of a game round needed to verify the integrity chain (HU-20). Decouples the
 * use case from the persistence entity: the infrastructure adapter maps the JPA row into this record.
 */
public record AuditRound(
        long id,
        long playerId,
        long gameId,
        long betCents,
        long winCents,
        long balancePostCents,
        long rngSeed,
        String prevHash,
        String rowHash) {
}
