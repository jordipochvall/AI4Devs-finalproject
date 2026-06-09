package com.novacasino.api.operator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.operator.dto.ReplayDto;
import com.novacasino.api.operator.exception.RoundNotFoundException;
import com.novacasino.api.player.dto.SpinResultDto;
import com.novacasino.api.player.dto.SpinResultDto.FreeSpinsDto;
import com.novacasino.api.player.dto.WinningPaylineDto;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Deterministic replay (readme §2.5.3, §4.4.5): reads the <strong>immutable record</strong> of a
 * round and returns it for the client to render as-is — it never recomputes the spin (no engine-drift
 * gating; AC4). For a triggering round it reconstructs {@code result.freeSpins.rounds[]} from the
 * child rows; for a free-spin child it renders that spin in isolation.
 */
@Service
public class ReplayService {

    private final GameRoundJpaRepository roundRepo;
    private final GameConfigJpaRepository configRepo;
    private final ObjectMapper objectMapper;

    public ReplayService(final GameRoundJpaRepository roundRepo,
                         final GameConfigJpaRepository configRepo, final ObjectMapper objectMapper) {
        this.roundRepo = roundRepo;
        this.configRepo = configRepo;
        this.objectMapper = objectMapper;
    }

    /** Returns the immutable replay record of a round owned by the operator. */
    @Transactional(readOnly = true)
    public ReplayDto getReplay(final Long roundId, final Long operatorId) {
        final GameRoundEntity round = roundRepo.findById(roundId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new RoundNotFoundException(roundId));

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
    }

    // -------------------------------------------------------------------------

    /** Maps a stored round (+ its already-built free-spin children) to the authoritative SpinResult. */
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
