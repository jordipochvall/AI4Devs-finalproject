package com.novacasino.application.player;

import com.novacasino.application.player.exception.LimitReachedException;
import com.novacasino.application.player.exception.SelfExcludedException;
import com.novacasino.common.dto.LimitDto;
import com.novacasino.common.dto.SelfExclusionDto;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Responsible-gaming enforcement (HU-19): server-side limits and self-exclusion. Pure use case over
 * {@link ResponsibleGamingPort}. The spin path calls {@link #assertCanSpin} before any wallet effect.
 * Hardening a limit applies immediately; relaxing it is deferred by a configurable cooldown.
 */
public class ResponsibleGamingUseCase {

    private static final String LOSS = "LOSS";

    private final ResponsibleGamingPort store;
    private final long cooldownSeconds;

    public ResponsibleGamingUseCase(final ResponsibleGamingPort store, final long cooldownSeconds) {
        this.store = store;
        this.cooldownSeconds = cooldownSeconds;
    }

    /** Sets/changes a limit: lowering (or first set) applies now; raising is deferred (AC3). */
    @Transactional
    public LimitDto setLimit(final Long userId, final String limitType, final String period,
                             final long amountCents) {
        final OffsetDateTime now = OffsetDateTime.now();
        final PlayerLimit existing = store.findLimit(userId, limitType, period)
                .map(this::promoteIfDue)
                .orElse(null);

        if (existing == null) {
            return toDto(store.createLimit(userId, limitType, period, amountCents, now));
        }
        if (amountCents <= existing.amountCents()) {
            // Hardening: immediate, drop any staged relaxation.
            return toDto(store.updateLimit(existing.id(), amountCents, now, null, null));
        }
        // Relaxing: deferred by the cooldown.
        return toDto(store.updateLimit(existing.id(), existing.amountCents(), existing.effectiveAt(),
                amountCents, now.plusSeconds(cooldownSeconds)));
    }

    /** Registers a self-exclusion period of {@code days} starting now (AC1). */
    @Transactional
    public SelfExclusionDto setSelfExclusion(final Long userId, final int days) {
        final OffsetDateTime start = OffsetDateTime.now();
        return store.createSelfExclusion(userId, start, start.plusDays(days));
    }

    /** Server-side gate before a spin (AC2/AC4): throws if self-excluded or a loss limit is reached. */
    @Transactional
    public void assertCanSpin(final Long userId) {
        final OffsetDateTime now = OffsetDateTime.now();
        if (store.isSelfExcluded(userId, now)) {
            throw new SelfExcludedException();
        }
        for (final PlayerLimit limit : store.findLimitsByUser(userId)) {
            if (!LOSS.equals(limit.limitType())) {
                continue;
            }
            final PlayerLimit effective = promoteIfDue(limit);
            final long loss = store.netLossSince(userId, windowStart(now, effective.period()));
            if (loss >= effective.amountCents()) {
                throw new LimitReachedException();
            }
        }
    }

    // -------------------------------------------------------------------------

    /** Promotes a staged relaxation whose cooldown has elapsed (persisted). */
    private PlayerLimit promoteIfDue(final PlayerLimit limit) {
        final OffsetDateTime due = limit.pendingEffectiveAt();
        if (due != null && !due.isAfter(OffsetDateTime.now())) {
            return store.updateLimit(limit.id(), limit.pendingAmountCents(), due, null, null);
        }
        return limit;
    }

    /** Rolling window start for a period (DAILY=24h, WEEKLY=7d, MONTHLY=30d). */
    private OffsetDateTime windowStart(final OffsetDateTime now, final String period) {
        return switch (period) {
            case "WEEKLY"  -> now.minus(Duration.ofDays(7));
            case "MONTHLY" -> now.minus(Duration.ofDays(30));
            default        -> now.minus(Duration.ofDays(1));
        };
    }

    private LimitDto toDto(final PlayerLimit l) {
        return new LimitDto(l.limitType(), l.period(), l.amountCents(), l.effectiveAt(),
                l.pendingAmountCents(), l.pendingEffectiveAt());
    }
}
