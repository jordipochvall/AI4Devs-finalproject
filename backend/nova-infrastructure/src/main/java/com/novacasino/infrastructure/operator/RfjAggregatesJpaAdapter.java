package com.novacasino.infrastructure.operator;

import com.novacasino.application.operator.RfjAggregatesPort;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/** JPA adapter for {@link RfjAggregatesPort} (HU-21): monthly {@code game_rounds} aggregates. */
@Component
public class RfjAggregatesJpaAdapter implements RfjAggregatesPort {

    private final GameRoundJpaRepository roundRepo;

    public RfjAggregatesJpaAdapter(final GameRoundJpaRepository roundRepo) {
        this.roundRepo = roundRepo;
    }

    @Override
    public RfjAggregates aggregate(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        return new RfjAggregates(
                roundRepo.sumBetCents(operatorId, from, to),
                roundRepo.sumWinCents(operatorId, from, to),
                roundRepo.countRounds(operatorId, from, to),
                roundRepo.countActivePlayers(operatorId, from, to));
    }
}
