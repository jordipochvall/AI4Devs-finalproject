package com.novacasino.api.player;

import com.novacasino.application.player.ResponsibleGamingUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.idempotency.IdempotencyService;
import com.novacasino.api.player.MaterializingSink.MechSpin;
import com.novacasino.common.dto.SpinResultDto;
import com.novacasino.common.dto.SpinResultDto.FreeSpinsDto;
import com.novacasino.common.dto.WinningPaylineDto;
import com.novacasino.api.player.exception.ConcurrentSpinException;
import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.api.player.exception.InsufficientBalanceException;
import com.novacasino.api.player.exception.InvalidBetException;
import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.engine.SpinKernel;
import com.novacasino.domain.rng.RngEngine;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.entity.JackpotPoolEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates a spin (readme §2.1.6): loads the wallet, validates the bet, runs the
 * {@link SpinKernel} through a {@link MaterializingSink}, debits the bet, credits the prize and
 * persists {@code game_rounds} + {@code wallet_transactions} atomically. The whole effect runs inside
 * one transaction (via {@link IdempotencyService#execute}); a wallet optimistic-lock conflict is
 * retried a few times and, if it persists, surfaces as a 409.
 */
@Service
public class SpinService {

    /** Wallet optimistic-lock retries before giving up with a 409 (AC8). */
    private static final int MAX_ATTEMPTS = 3;

    private final GameJpaRepository gameRepo;
    private final GameConfigJpaRepository configRepo;
    private final WalletJpaRepository walletRepo;
    private final WalletTransactionJpaRepository txRepo;
    private final GameRoundJpaRepository roundRepo;
    private final GameConfigMapper configMapper;
    private final GameCompiler compiler;
    private final RngFactory rngFactory;
    private final IdempotencyService idempotency;
    private final ResponsibleGamingUseCase responsibleGaming;
    private final JackpotService jackpotService;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public SpinService(final GameJpaRepository gameRepo, final GameConfigJpaRepository configRepo,
                       final WalletJpaRepository walletRepo, final WalletTransactionJpaRepository txRepo,
                       final GameRoundJpaRepository roundRepo, final GameConfigMapper configMapper,
                       final GameCompiler compiler, final RngFactory rngFactory,
                       final IdempotencyService idempotency,
                       final ResponsibleGamingUseCase responsibleGaming,
                       final JackpotService jackpotService, final ObjectMapper objectMapper) {
        this.gameRepo = gameRepo;
        this.configRepo = configRepo;
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
        this.roundRepo = roundRepo;
        this.configMapper = configMapper;
        this.compiler = compiler;
        this.rngFactory = rngFactory;
        this.idempotency = idempotency;
        this.responsibleGaming = responsibleGaming;
        this.jackpotService = jackpotService;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes a spin idempotently, retrying on wallet optimistic-lock conflicts.
     *
     * @param userId     authenticated player
     * @param operatorId player's operator
     * @param gameId     game to play
     * @param idemKey    Idempotency-Key header value
     * @param betCents   total bet in cents
     * @param currency   bet currency (part of the idempotency payload)
     * @return the spin result (or the replayed original on a retry with the same key)
     */
    public SpinResultDto spin(final Long userId, final Long operatorId, final Long gameId,
                              final UUID idemKey, final long betCents, final String currency) {
        final SpinPayload payload = new SpinPayload(gameId, betCents, currency);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                return idempotency.execute(userId, "spin", idemKey, payload, SpinResultDto.class,
                        () -> doSpin(userId, operatorId, gameId, betCents));
            } catch (final OptimisticLockingFailureException conflict) {
                // Another spin/recharge changed the balance first; retry with a fresh transaction.
            }
        }
        throw new ConcurrentSpinException();
    }

    // -------------------------------------------------------------------------

    /**
     * The actual side-effecting spin. It runs inside the transaction opened by
     * {@link IdempotencyService#execute} (a separate, proxied bean), so the whole spin + idempotency
     * key insertion commit or roll back together; no own {@code @Transactional} is needed here.
     */
    SpinResultDto doSpin(final Long userId, final Long operatorId, final Long gameId, final long betCents) {
        // HU-19: server-side responsible-gaming gate — refuse before any wallet effect or round.
        responsibleGaming.assertCanSpin(userId);

        final GameEntity game = activeGame(gameId);
        final GameConfigEntity config = configRepo.findById(game.getActiveConfigId())
                .orElseThrow(() -> new GameNotFoundException(gameId));
        final CompiledGame compiled = compiler.compile(config.getId(),
                configMapper.toSpec(parse(config.getConfig())));
        final int paylineCount = compiled.paylineCount();
        validateBet(game, paylineCount, betCents);

        final WalletEntity wallet = fundedWallet(userId, betCents);
        final long balancePre = wallet.getBalanceCents();

        // Run the engine through the production sink (same kernel as the simulator).
        final long seed = secureRandom.nextLong();
        final List<MechSpin> spins = runEngine(compiled, betCents, seed);
        final MechSpin base = spins.get(0);

        // Progressive jackpot (HU-26): deterministic in the seed; the win is attributed to the base round.
        final JackpotOutcome jackpot = computeJackpot(gameId, betCents, seed);

        final long baseWin = base.winCents() + jackpot.win();
        final long totalWin = spins.stream().mapToLong(MechSpin::winCents).sum() + jackpot.win();
        final long balancePost = balancePre - betCents + totalWin;
        final long lineBet = betCents / paylineCount;
        final long balanceAfterBase = balancePre - betCents + baseWin;

        // Update the balance (optimistic lock via @Version).
        wallet.setBalanceCents(balancePost);
        walletRepo.save(wallet);

        // Persist the base round (including any jackpot win), settle the pool, then the free-spin children.
        final GameRoundEntity baseRound = roundRepo.save(GameRoundEntity.baseRound(
                operatorId, userId, gameId, config.getId(), seed, betCents, baseWin,
                balancePre, balanceAfterBase, toJson(resultOf(base))));
        settleJackpot(jackpot, operatorId, gameId, baseRound.getId(), userId);

        final List<SpinResultDto> freeSpinRounds = persistFreeSpins(
                spins, balanceAfterBase, lineBet, operatorId, userId, gameId, config.getId(), seed, baseRound.getId());

        recordLedger(wallet, betCents, totalWin, balancePre, balancePost, baseRound.getId());

        // Top-level result: base spin view/lines, but the round's TOTAL win and final balance.
        final int awarded = compiled.freeSpinsAwardedFor(base.scatterCount());
        final FreeSpinsDto freeSpins = new FreeSpinsDto(awarded > 0, awarded, freeSpinRounds);
        return new SpinResultDto(baseRound.getId(), betCents, lineBet, totalWin, balancePre, balancePost,
                base.view(), base.winningPaylines(), base.scatterCount(), freeSpins);
    }

    /** Loads an active, playable game (active flag + an active config) or fails with 404. */
    private GameEntity activeGame(final Long gameId) {
        final GameEntity game = gameRepo.findByIdAndActiveTrue(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        if (game.getActiveConfigId() == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }

    /** Loads the player's wallet and asserts it can cover the bet (else 422). */
    private WalletEntity fundedWallet(final Long userId, final long betCents) {
        final WalletEntity wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
        if (betCents > wallet.getBalanceCents()) {
            throw new InsufficientBalanceException(betCents, wallet.getBalanceCents());
        }
        return wallet;
    }

    /** Runs the kernel (same engine as the simulator) and returns the base spin plus any free spins. */
    private List<MechSpin> runEngine(final CompiledGame compiled, final long betCents, final long seed) {
        final RngEngine rng = rngFactory.create(seed);
        final MaterializingSink sink = new MaterializingSink(compiled);
        new SpinKernel(compiled).spin(betCents, rng, sink);
        return sink.spins();
    }

    /** Computes the (deterministic) jackpot contribution and grant for this spin. */
    private JackpotOutcome computeJackpot(final Long gameId, final long betCents, final long seed) {
        final Optional<JackpotPoolEntity> poolOpt = jackpotService.findPool(gameId);
        if (poolOpt.isEmpty()) {
            return new JackpotOutcome(poolOpt, false, 0L, 0L);
        }
        final JackpotPoolEntity pool = poolOpt.get();
        final long contribution = jackpotService.contribution(pool, betCents);
        final boolean awarded = JackpotService.isAwarded(seed, pool.getOddsDenominator());
        final long win = awarded ? pool.getCurrentCents() + contribution : 0L;
        return new JackpotOutcome(poolOpt, awarded, contribution, win);
    }

    /** Applies the jackpot outcome to the pool and records the grant, once the base round exists. */
    private void settleJackpot(final JackpotOutcome jackpot, final Long operatorId, final Long gameId,
                               final Long baseRoundId, final Long userId) {
        if (jackpot.pool().isEmpty()) {
            return;
        }
        jackpotService.applyOutcome(jackpot.pool().get(), jackpot.awarded(), jackpot.contribution());
        if (jackpot.awarded()) {
            jackpotService.recordGrant(operatorId, gameId, baseRoundId, userId, jackpot.win());
        }
    }

    /** Persists each free-spin child round (chaining the balance) and returns their result DTOs. */
    private List<SpinResultDto> persistFreeSpins(final List<MechSpin> spins, final long balanceAfterBase,
                                                 final long lineBet, final Long operatorId, final Long userId,
                                                 final Long gameId, final Long configId, final long seed,
                                                 final Long baseRoundId) {
        final int totalFreeSpins = spins.size() - 1;
        final List<SpinResultDto> freeSpinRounds = new ArrayList<>(totalFreeSpins);
        long running = balanceAfterBase;
        for (int i = 1; i < spins.size(); i++) {
            final MechSpin fs = spins.get(i);
            final long pre = running;
            final long post = running + fs.winCents();
            running = post;
            final int remainingAfter = totalFreeSpins - i;
            final GameRoundEntity child = roundRepo.save(GameRoundEntity.freeSpinRound(
                    operatorId, userId, gameId, configId, seed, fs.winCents(),
                    pre, post, toJson(resultOf(fs)), baseRoundId, remainingAfter));
            freeSpinRounds.add(toDto(child.getId(), 0L, lineBet, fs, pre, post, noFreeSpins()));
        }
        return freeSpinRounds;
    }

    /** Records the ledger movements: one BET and (if any prize) one WIN, tied to the base round. */
    private void recordLedger(final WalletEntity wallet, final long betCents, final long totalWin,
                              final long balancePre, final long balancePost, final Long baseRoundId) {
        txRepo.save(WalletTransactionEntity.bet(wallet.getId(), betCents, balancePre - betCents, baseRoundId));
        if (totalWin > 0) {
            txRepo.save(WalletTransactionEntity.win(wallet.getId(), totalWin, balancePost, baseRoundId));
        }
    }

    /** The resolved progressive-jackpot outcome of a spin: the pool (if any), the grant flag and amounts. */
    private record JackpotOutcome(Optional<JackpotPoolEntity> pool, boolean awarded, long contribution, long win) {
    }

    /** Validates the bet against the game's commercial range, step and the payline-multiple rule. */
    private void validateBet(final GameEntity game, final int paylineCount, final long betCents) {
        if (betCents < game.getMinBetCents() || betCents > game.getMaxBetCents()) {
            throw new InvalidBetException();
        }
        if ((betCents - game.getMinBetCents()) % game.getBetStepCents() != 0) {
            throw new InvalidBetException();
        }
        if (paylineCount > 0 && betCents % paylineCount != 0) {
            throw new InvalidBetException();
        }
    }

    /** Builds a free-spin {@link SpinResultDto} (no nested free spins, no wager). */
    private SpinResultDto toDto(final Long roundId, final long betCents, final long lineBet,
                               final MechSpin spin, final long balancePre, final long balancePost,
                               final FreeSpinsDto freeSpins) {
        return new SpinResultDto(roundId, betCents, lineBet, spin.winCents(), balancePre, balancePost,
                spin.view(), spin.winningPaylines(), spin.scatterCount(), freeSpins);
    }

    private FreeSpinsDto noFreeSpins() {
        return new FreeSpinsDto(false, 0, List.of());
    }

    /** The mechanical part of a spin persisted as {@code game_rounds.result} (§3.2.8). */
    private RoundResult resultOf(final MechSpin spin) {
        return new RoundResult(spin.view(), spin.winningPaylines(), spin.scatterCount(), spin.multiplier());
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Invalid config JSON in database", e);
        }
    }

    private String toJson(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize round result", e);
        }
    }

    /** Idempotency payload: reusing the same key with a different game/bet/currency → 409. */
    private record SpinPayload(Long gameId, long betCents, String currency) {
    }

    /** Shape of {@code game_rounds.result}: only the visual/mechanical part of a spin. */
    private record RoundResult(List<List<String>> view, List<WinningPaylineDto> winningPaylines,
                               int scatterCount, int multiplier) {
    }
}
