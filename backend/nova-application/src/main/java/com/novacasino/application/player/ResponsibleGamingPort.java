package com.novacasino.application.player;

import com.novacasino.common.dto.SelfExclusionDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** Output port for responsible-gaming persistence and loss queries (HU-19). */
public interface ResponsibleGamingPort {

    Optional<PlayerLimit> findLimit(Long userId, String limitType, String period);

    List<PlayerLimit> findLimitsByUser(Long userId);

    PlayerLimit createLimit(Long userId, String limitType, String period, long amountCents,
                            OffsetDateTime effectiveAt);

    PlayerLimit updateLimit(Long id, long amountCents, OffsetDateTime effectiveAt,
                            Long pendingAmountCents, OffsetDateTime pendingEffectiveAt);

    boolean isSelfExcluded(Long userId, OffsetDateTime instant);

    SelfExclusionDto createSelfExclusion(Long userId, OffsetDateTime start, OffsetDateTime end);

    long netLossSince(Long userId, OffsetDateTime since);
}
