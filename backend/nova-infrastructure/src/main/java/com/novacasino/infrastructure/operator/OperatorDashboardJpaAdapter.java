package com.novacasino.infrastructure.operator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.application.operator.OperatorDashboardPort;
import com.novacasino.common.dto.DashboardDto;
import com.novacasino.common.dto.DashboardDto.TopGameDto;
import com.novacasino.common.dto.RoundDetailDto;
import com.novacasino.common.dto.WinningPaylineDto;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JPA adapter for {@link OperatorDashboardPort} (HU-16): aggregates {@code game_rounds} over a date
 * window and reads a single round's detail from the immutable row. All reads are operator-scoped.
 */
@Component
public class OperatorDashboardJpaAdapter implements OperatorDashboardPort {

    /** How many games to include in the "most-played" ranking. */
    private static final int TOP_GAMES_LIMIT = 5;
    private static final OffsetDateTime MIN_INSTANT = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final GameRoundJpaRepository roundRepo;
    private final GameJpaRepository gameRepo;
    private final ObjectMapper objectMapper;

    public OperatorDashboardJpaAdapter(final GameRoundJpaRepository roundRepo,
                                       final GameJpaRepository gameRepo, final ObjectMapper objectMapper) {
        this.roundRepo = roundRepo;
        this.gameRepo = gameRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public DashboardDto dashboard(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        // Null bounds default to a wide window so the JPQL never binds null timestamps.
        final OffsetDateTime effFrom = from != null ? from : MIN_INSTANT;
        final OffsetDateTime effTo = to != null ? to : OffsetDateTime.now();

        final long activePlayers = roundRepo.countActivePlayers(operatorId, effFrom, effTo);
        final long ggrCents = roundRepo.ggrCents(operatorId, effFrom, effTo);
        final long totalRounds = roundRepo.countRounds(operatorId, effFrom, effTo);

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

    @Override
    public Optional<RoundDetailDto> roundDetail(final Long roundId, final Long operatorId) {
        return roundRepo.findById(roundId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .map(this::toDetail);
    }

    private RoundDetailDto toDetail(final GameRoundEntity round) {
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
