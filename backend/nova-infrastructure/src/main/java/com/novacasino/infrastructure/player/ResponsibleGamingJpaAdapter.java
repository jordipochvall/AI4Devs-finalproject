package com.novacasino.infrastructure.player;

import com.novacasino.application.player.PlayerLimit;
import com.novacasino.application.player.ResponsibleGamingPort;
import com.novacasino.common.dto.SelfExclusionDto;
import com.novacasino.infrastructure.persistence.entity.PlayerLimitEntity;
import com.novacasino.infrastructure.persistence.entity.SelfExclusionEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.PlayerLimitJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SelfExclusionJpaRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** JPA adapter for {@link ResponsibleGamingPort} (HU-19). */
@Component
public class ResponsibleGamingJpaAdapter implements ResponsibleGamingPort {

    private final PlayerLimitJpaRepository limitRepo;
    private final SelfExclusionJpaRepository exclusionRepo;
    private final GameRoundJpaRepository roundRepo;

    public ResponsibleGamingJpaAdapter(final PlayerLimitJpaRepository limitRepo,
                                       final SelfExclusionJpaRepository exclusionRepo,
                                       final GameRoundJpaRepository roundRepo) {
        this.limitRepo = limitRepo;
        this.exclusionRepo = exclusionRepo;
        this.roundRepo = roundRepo;
    }

    @Override
    public Optional<PlayerLimit> findLimit(final Long userId, final String limitType, final String period) {
        return limitRepo.findByUserIdAndLimitTypeAndPeriod(userId, limitType, period).map(this::toModel);
    }

    @Override
    public List<PlayerLimit> findLimitsByUser(final Long userId) {
        return limitRepo.findByUserId(userId).stream().map(this::toModel).toList();
    }

    @Override
    public PlayerLimit createLimit(final Long userId, final String limitType, final String period,
                                   final long amountCents, final OffsetDateTime effectiveAt) {
        return toModel(limitRepo.save(new PlayerLimitEntity(userId, limitType, period, amountCents, effectiveAt)));
    }

    @Override
    public PlayerLimit updateLimit(final Long id, final long amountCents, final OffsetDateTime effectiveAt,
                                   final Long pendingAmountCents, final OffsetDateTime pendingEffectiveAt) {
        final PlayerLimitEntity e = limitRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Limit not found: " + id));
        e.setAmountCents(amountCents);
        e.setEffectiveAt(effectiveAt);
        e.setPendingAmountCents(pendingAmountCents);
        e.setPendingEffectiveAt(pendingEffectiveAt);
        e.setUpdatedAt(OffsetDateTime.now());
        return toModel(limitRepo.save(e));
    }

    @Override
    public boolean isSelfExcluded(final Long userId, final OffsetDateTime instant) {
        return exclusionRepo.existsByUserIdAndEndAtAfter(userId, instant);
    }

    @Override
    public SelfExclusionDto createSelfExclusion(final Long userId, final OffsetDateTime start, final OffsetDateTime end) {
        final SelfExclusionEntity saved = exclusionRepo.save(new SelfExclusionEntity(userId, start, end));
        return new SelfExclusionDto(saved.getStartAt(), saved.getEndAt());
    }

    @Override
    public long netLossSince(final Long userId, final OffsetDateTime since) {
        return roundRepo.netLossSince(userId, since);
    }

    private PlayerLimit toModel(final PlayerLimitEntity e) {
        return new PlayerLimit(e.getId(), e.getUserId(), e.getLimitType(), e.getPeriod(),
                e.getAmountCents(), e.getEffectiveAt(), e.getPendingAmountCents(), e.getPendingEffectiveAt());
    }
}
