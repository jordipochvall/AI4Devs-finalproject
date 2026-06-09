package com.novacasino.api.operator;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.operator.dto.RoundSummaryDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Operator audit queries (readme §3.2.8): a paginated, filterable view of {@code game_rounds} for
 * resolving claims. Filters are optional and combine with AND; the default order is
 * {@code created_at DESC}, served by the {@code idx_game_rounds_*_created} indexes.
 */
@Service
public class AuditService {

    private final GameRoundJpaRepository roundRepo;

    public AuditService(final GameRoundJpaRepository roundRepo) {
        this.roundRepo = roundRepo;
    }

    /**
     * Paginated audit search within the operator.
     *
     * @param operatorId the operator scope (from the token)
     * @param playerId   optional player filter
     * @param gameId     optional game filter
     * @param from       optional lower bound on {@code created_at}
     * @param to         optional upper bound on {@code created_at}
     * @param pageable   page/size/sort (defaults to {@code created_at DESC} when unsorted)
     */
    @Transactional(readOnly = true)
    public PageResponse<RoundSummaryDto> searchRounds(final Long operatorId, final Long playerId,
                                                      final Long gameId, final OffsetDateTime from,
                                                      final OffsetDateTime to, final Pageable pageable) {
        final Pageable effective = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
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
        return PageResponse.of(roundRepo.findAll(spec, effective), this::toSummary);
    }

    private RoundSummaryDto toSummary(final GameRoundEntity r) {
        return new RoundSummaryDto(
                r.getId(), r.getPlayerId(), r.getGameId(), r.getGameConfigId(),
                r.getBetCents(), r.getWinCents(), r.getBalancePostCents(),
                r.isFreeSpin(), r.getTriggeringRoundId(), r.getCreatedAt());
    }
}
