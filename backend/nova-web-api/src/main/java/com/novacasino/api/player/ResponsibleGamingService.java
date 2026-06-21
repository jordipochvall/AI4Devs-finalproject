package com.novacasino.api.player;

import com.novacasino.api.player.dto.LimitDto;
import com.novacasino.api.player.dto.SelfExclusionDto;
import com.novacasino.api.player.exception.LimitReachedException;
import com.novacasino.api.player.exception.SelfExcludedException;
import com.novacasino.infrastructure.persistence.entity.PlayerLimitEntity;
import com.novacasino.infrastructure.persistence.entity.SelfExclusionEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.PlayerLimitJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SelfExclusionJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Responsible-gaming enforcement (HU-19, decision D7): server-side limits and self-exclusion. The
 * spin path calls {@link #assertCanSpin} before any wallet effect, so a reached loss limit or an active
 * self-exclusion refuses the spin with no balance change and no round recorded. Hardening a limit
 * applies immediately; relaxing it is deferred by a configurable cooldown.
 */
@Service
public class ResponsibleGamingService {

    private static final String LOSS = "LOSS";

    private final PlayerLimitJpaRepository limitRepo;
    private final SelfExclusionJpaRepository exclusionRepo;
    private final GameRoundJpaRepository roundRepo;
    private final long cooldownSeconds;

    public ResponsibleGamingService(final PlayerLimitJpaRepository limitRepo,
                                    final SelfExclusionJpaRepository exclusionRepo,
                                    final GameRoundJpaRepository roundRepo,
                                    @Value("${app.responsible-gaming.cooldown-seconds:86400}") final long cooldownSeconds) {
        this.limitRepo       = limitRepo;
        this.exclusionRepo   = exclusionRepo;
        this.roundRepo       = roundRepo;
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Sets or changes a player's limit. Lowering (or first set) applies immediately; raising the limit
     * is staged in {@code pending_*} and only takes effect after the cooldown (AC3).
     */
    @Transactional
    public LimitDto setLimit(final Long userId, final String limitType, final String period,
                             final long amountCents) {
        final OffsetDateTime now = OffsetDateTime.now();
        final PlayerLimitEntity limit = limitRepo
                .findByUserIdAndLimitTypeAndPeriod(userId, limitType, period)
                .map(this::promoteIfDue)
                .orElse(null);

        if (limit == null) {
            return toDto(limitRepo.save(new PlayerLimitEntity(userId, limitType, period, amountCents, now)));
        }

        if (amountCents <= limit.getAmountCents()) {
            // Hardening: immediate, and any staged relaxation is dropped.
            limit.setAmountCents(amountCents);
            limit.setEffectiveAt(now);
            limit.setPendingAmountCents(null);
            limit.setPendingEffectiveAt(null);
        } else {
            // Relaxing: deferred by the cooldown.
            limit.setPendingAmountCents(amountCents);
            limit.setPendingEffectiveAt(now.plusSeconds(cooldownSeconds));
        }
        limit.setUpdatedAt(now);
        return toDto(limitRepo.save(limit));
    }

    /** Registers a self-exclusion period of {@code days} starting now (AC1). */
    @Transactional
    public SelfExclusionDto setSelfExclusion(final Long userId, final int days) {
        final OffsetDateTime start = OffsetDateTime.now();
        final SelfExclusionEntity saved = exclusionRepo.save(
                new SelfExclusionEntity(userId, start, start.plusDays(days)));
        return new SelfExclusionDto(saved.getStartAt(), saved.getEndAt());
    }

    /**
     * Server-side gate invoked before a spin (AC2/AC4): throws if the player is self-excluded or has
     * reached a loss limit. Runs inside the spin transaction, so a throw leaves no side effects.
     */
    @Transactional
    public void assertCanSpin(final Long userId) {
        final OffsetDateTime now = OffsetDateTime.now();
        if (exclusionRepo.existsByUserIdAndEndAtAfter(userId, now)) {
            throw new SelfExcludedException();
        }
        for (final PlayerLimitEntity limit : limitRepo.findByUserId(userId)) {
            if (!LOSS.equals(limit.getLimitType())) {
                continue; // only loss limits gate the spin in this iteration
            }
            promoteIfDue(limit);
            final long loss = roundRepo.netLossSince(userId, windowStart(now, limit.getPeriod()));
            if (loss >= limit.getAmountCents()) {
                throw new LimitReachedException();
            }
        }
    }

    // -------------------------------------------------------------------------

    /** Promotes a staged relaxation whose cooldown has elapsed (persisted). */
    private PlayerLimitEntity promoteIfDue(final PlayerLimitEntity limit) {
        final OffsetDateTime due = limit.getPendingEffectiveAt();
        if (due != null && !due.isAfter(OffsetDateTime.now())) {
            limit.setAmountCents(limit.getPendingAmountCents());
            limit.setEffectiveAt(due);
            limit.setPendingAmountCents(null);
            limit.setPendingEffectiveAt(null);
            limitRepo.save(limit);
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

    private LimitDto toDto(final PlayerLimitEntity l) {
        return new LimitDto(l.getLimitType(), l.getPeriod(), l.getAmountCents(), l.getEffectiveAt(),
                l.getPendingAmountCents(), l.getPendingEffectiveAt());
    }
}
