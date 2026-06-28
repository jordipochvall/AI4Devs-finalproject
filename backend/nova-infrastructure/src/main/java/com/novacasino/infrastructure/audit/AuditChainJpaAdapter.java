package com.novacasino.infrastructure.audit;

import com.novacasino.application.audit.AuditChainPort;
import com.novacasino.application.audit.AuditRound;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * JPA adapter (hexagonal) for {@link AuditChainPort}: reads the operator's rounds in chain order from
 * {@code game_rounds} and maps each entity into the application's {@link AuditRound} (HU-20).
 */
@Component
public class AuditChainJpaAdapter implements AuditChainPort {

    private static final Logger log = LoggerFactory.getLogger(AuditChainJpaAdapter.class);

    private final GameRoundJpaRepository roundRepo;

    public AuditChainJpaAdapter(final GameRoundJpaRepository roundRepo) {
        this.roundRepo = roundRepo;
    }

    @Override
    public List<AuditRound> roundsInWindow(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        final List<AuditRound> rounds = roundRepo
                .findByOperatorIdAndCreatedAtBetweenOrderByIdAsc(operatorId, from, to).stream()
                .map(r -> new AuditRound(r.getId(), r.getPlayerId(), r.getGameId(),
                        r.getBetCents(), r.getWinCents(), r.getBalancePostCents(), r.getRngSeed(),
                        r.getPrevHash(), r.getRowHash()))
                .toList();
        log.debug("Loaded {} rounds for integrity verification: operatorId={}, window=[{}, {}]",
                rounds.size(), operatorId, from, to);
        return rounds;
    }
}
