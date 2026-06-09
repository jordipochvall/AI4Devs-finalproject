package com.novacasino.domain.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-1-BE-01 · AC1 + AC7 — the compiler turns a config spec into primitive structures and caches
 * the compiled game by {@code configId}.
 */
class GameCompilerTest {

    private final GameCompiler compiler = new GameCompiler();

    @Test
    void compilesSymbolsReelsPaylinesAndPaytableToPrimitives() {
        final CompiledGame game = compiler.compile(1L, EngineTestSupport.egyptianSpec());

        assertThat(game.cols()).isEqualTo(5);
        assertThat(game.rows()).isEqualTo(3);
        assertThat(game.symbolCount()).isEqualTo(5);
        assertThat(game.paylineCount()).isEqualTo(5);

        // Symbols became dense int ids preserving spec order.
        assertThat(game.symbolId(0)).isEqualTo("WILD");
        assertThat(game.kind(0)).isEqualTo(SymbolKind.WILD);
        assertThat(game.kind(1)).isEqualTo(SymbolKind.SCATTER);
        assertThat(game.kind(2)).isEqualTo(SymbolKind.REGULAR);

        // Reels are int[][] of the right shape.
        assertThat(game.reels.length).isEqualTo(5);
        assertThat(game.reels[0].length).isEqualTo(8);

        // Paytable became long[][] indexed by [symbolId][count].
        final int anubis = 2;
        assertThat(game.linePayouts[anubis][3]).isEqualTo(10L);
        assertThat(game.linePayouts[anubis][5]).isEqualTo(250L);

        // Best regular at full length is ANUBIS (250).
        assertThat(game.bestRegularByCount[5]).isEqualTo(250L);

        // Free spins compiled.
        assertThat(game.hasFreeSpins).isTrue();
        assertThat(game.fsMinTriggerCount).isEqualTo(3);
        assertThat(game.fsAward[3]).isEqualTo(8);
        assertThat(game.fsMultiplier).isEqualTo(2);
        assertThat(game.fsRetrigger).isTrue();
        assertThat(game.wildSubstitutesRegular).isTrue();
    }

    @Test
    void cachesCompiledGameByConfigId() {
        final GameConfigSpec spec = EngineTestSupport.egyptianSpec();
        final CompiledGame first = compiler.compile(42L, spec);
        final CompiledGame second = compiler.compile(42L, spec);

        assertThat(second).isSameAs(first); // cached, not recompiled
    }
}
