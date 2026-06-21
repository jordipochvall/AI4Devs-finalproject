package com.novacasino.api.operator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.operator.dto.DashboardDto;
import com.novacasino.api.operator.dto.DashboardDto.TopGameDto;
import com.novacasino.api.operator.dto.RoundDetailDto;
import com.novacasino.api.operator.exception.RoundNotFoundException;
import com.novacasino.api.player.dto.WinningPaylineDto;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Operator activity dashboard (HU-16): aggregates {@code game_rounds} (active players, GGR, most-played
 * games) over an optional date window, and serves a single round's detail. All reads are scoped to the
 * operator of the token (AC3 isolation).
 */
@Service
public class OperatorDashboardService {

    /** How many games to include in the "most-played" ranking. */
    private static final int TOP_GAMES_LIMIT = 5;
    private static final OffsetDateTime MIN_INSTANT = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final GameRoundJpaRepository roundRepo;
    private final GameJpaRepository gameRepo;
    private final ObjectMapper objectMapper;

    public OperatorDashboardService(final GameRoundJpaRepository roundRepo,
                                    final GameJpaRepository gameRepo, final ObjectMapper objectMapper) {
        this.roundRepo    = roundRepo;
        this.gameRepo     = gameRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Aggregated KPIs for the operator over [from, to]. Null bounds default to a wide window so the
     * JPQL never binds null timestamps (which Postgres cannot type-infer).
     */
    @Transactional(readOnly = true)
    public DashboardDto dashboard(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        final OffsetDateTime effFrom = from != null ? from : MIN_INSTANT;
        final OffsetDateTime effTo   = to   != null ? to   : OffsetDateTime.now();

        final long activePlayers = roundRepo.countActivePlayers(operatorId, effFrom, effTo);
        final long ggrCents      = roundRepo.ggrCents(operatorId, effFrom, effTo);
        final long totalRounds   = roundRepo.countRounds(operatorId, effFrom, effTo);

        final var rows = roundRepo.topGames(operatorId, effFrom, effTo, PageRequest.of(0, TOP_GAMES_LIMIT));
        final Map<Long, GameEntity> games = gameRepo
                .findAllById(rows.stream().map(r -> r.getGameId()).toList()).stream()
                .collect(Collectors.toMap(GameEntity::getId, Function.identity()));
        final List<TopGameDto> topGames = rows.stream()
                .map(r -> {
                    final GameEntity g = games.get(r.getGameId());
                    return new TopGameDto(r.getGameId(),
                            g != null ? g.getName() : null, g != null ? g.getTheme() : null, r.getRounds());
                })
                .toList();

        return new DashboardDto(effFrom, effTo, activePlayers, ggrCents, totalRounds, topGames);
    }

    /** Detail of a single round owned by the operator (AC2/AC4: 404 if missing or another operator). */
    @Transactional(readOnly = true)
    public RoundDetailDto roundDetail(final Long roundId, final Long operatorId) {
        final GameRoundEntity round = roundRepo.findById(roundId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new RoundNotFoundException(roundId));

        final JsonNode result = parse(round.getResult());
        final List<List<String>> view =
                objectMapper.convertValue(result.get("view"), new TypeReference<>() { });
        final List<WinningPaylineDto> winningPaylines = result.has("winningPaylines")
                ? objectMapper.convertValue(result.get("winningPaylines"), new TypeReference<>() { })
                : List.of();

        return new RoundDetailDto(round.getId(), round.getGameId(), round.getPlayerId(),
                round.getGameConfigId(), round.getBetCents(), round.getWinCents(),
                round.getBalancePreCents(), round.getBalancePostCents(), round.isFreeSpin(),
                round.getCreatedAt(), view, winningPaylines, result.path("scatterCount").asInt());
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final Exception e) {
            throw new IllegalStateException("Invalid JSON in database", e);
        }
    }
}
