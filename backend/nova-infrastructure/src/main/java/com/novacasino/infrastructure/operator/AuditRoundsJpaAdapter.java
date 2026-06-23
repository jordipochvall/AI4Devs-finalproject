package com.novacasino.infrastructure.operator;

import com.novacasino.application.operator.AuditRoundsPort;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.RoundSummaryDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.support.Pages;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** JPA adapter for {@link AuditRoundsPort} (HU-3): Criteria filters + createdAt DESC paging. */
@Component
public class AuditRoundsJpaAdapter implements AuditRoundsPort {

    private final GameRoundJpaRepository roundRepo;

    public AuditRoundsJpaAdapter(final GameRoundJpaRepository roundRepo) {
        this.roundRepo = roundRepo;
    }

    @Override
    public PageResponse<RoundSummaryDto> search(final Long operatorId, final Long playerId, final Long gameId,
                                                final OffsetDateTime from, final OffsetDateTime to,
                                                final PageRequestDto page) {
        final var pageable = PageRequest.of(page.page(), page.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        final Specification<GameRoundEntity> spec = (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("operatorId"), operatorId));
            if (playerId != null) {
                predicates.add(cb.equal(root.get("playerId"), playerId));
            }
            if (gameId != null) {
                predicates.add(cb.equal(root.get("gameId"), gameId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return Pages.of(roundRepo.findAll(spec, pageable), r -> new RoundSummaryDto(
                r.getId(), r.getPlayerId(), r.getGameId(), r.getGameConfigId(),
                r.getBetCents(), r.getWinCents(), r.getBalancePostCents(),
                r.isFreeSpin(), r.getTriggeringRoundId(), r.getCreatedAt()));
    }
}
