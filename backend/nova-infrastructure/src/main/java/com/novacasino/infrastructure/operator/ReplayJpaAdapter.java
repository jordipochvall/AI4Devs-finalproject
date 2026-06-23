package com.novacasino.infrastructure.operator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.application.math.exception.ConfigNotFoundException;
import com.novacasino.application.operator.ReplayPort;
import com.novacasino.common.dto.ReplayDto;
import com.novacasino.common.dto.SpinResultDto;
import com.novacasino.common.dto.SpinResultDto.FreeSpinsDto;
import com.novacasino.common.dto.WinningPaylineDto;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for {@link ReplayPort} (HU-3): reads the immutable round record and returns it as-is. It
 * reconstructs {@code freeSpins.rounds[]} from the child rows for a triggering round; a free-spin child
 * is rendered in isolation. Never recomputes the spin.
 */
@Component
public class ReplayJpaAdapter implements ReplayPort {

    private final GameRoundJpaRepository roundRepo;
    private final GameConfigJpaRepository configRepo;
    private final ObjectMapper objectMapper;

    public ReplayJpaAdapter(final GameRoundJpaRepository roundRepo,
                            final GameConfigJpaRepository configRepo, final ObjectMapper objectMapper) {
        this.roundRepo = roundRepo;
        this.configRepo = configRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ReplayDto> findReplay(final Long roundId, final Long operatorId) {
        return roundRepo.findById(roundId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .map(round -> {
                    // The exact config version used in the round (not the active one — AC2).
                    final GameConfigEntity config = configRepo.findById(round.getGameConfigId())
                            .orElseThrow(() -> new ConfigNotFoundException(round.getGameConfigId()));
                    final JsonNode configNode = parse(config.getConfig());
                    final int paylineCount = configNode.path("paylines").size();

                    // Reconstruct free-spin children only for a triggering (base) round (AC3).
                    final List<SpinResultDto> children = round.isFreeSpin()
                            ? List.of()
                            : roundRepo.findByTriggeringRoundIdOrderByIdAsc(roundId).stream()
                                    .map(child -> toResult(child, paylineCount, List.of()))
                                    .toList();

                    final SpinResultDto result = toResult(round, paylineCount, children);
                    return new ReplayDto(round.getId(), round.getGameId(), round.getGameConfigId(),
                            round.getRngSeed(), result, configNode);
                });
    }

    private SpinResultDto toResult(final GameRoundEntity round, final int paylineCount,
                                   final List<SpinResultDto> children) {
        final JsonNode r = parse(round.getResult());
        final List<List<String>> view =
                objectMapper.convertValue(r.get("view"), new TypeReference<>() { });
        final List<WinningPaylineDto> winningPaylines = r.has("winningPaylines")
                ? objectMapper.convertValue(r.get("winningPaylines"), new TypeReference<>() { })
                : List.of();
        final int scatterCount = r.path("scatterCount").asInt();
        final long lineBet = paylineCount > 0 ? round.getBetCents() / paylineCount : 0L;
        final FreeSpinsDto freeSpins = new FreeSpinsDto(!children.isEmpty(), children.size(), children);

        return new SpinResultDto(round.getId(), round.getBetCents(), lineBet, round.getWinCents(),
                round.getBalancePreCents(), round.getBalancePostCents(),
                view, winningPaylines, scatterCount, freeSpins);
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final Exception e) {
            throw new IllegalStateException("Invalid JSON in database", e);
        }
    }
}
