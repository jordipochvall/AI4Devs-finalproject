package com.novacasino.domain.rng;

/**
 * Random-number source consumed by the engine (port, hexagonal architecture).
 *
 * <p>The {@code SpinKernel} consumes it in a <strong>fixed order</strong> — one {@link #nextInt(int)}
 * per reel, in column order {@code 0 → cols-1}, with bonus features consuming afterwards — so that a
 * given seed yields the same spin sequence on any machine (readme §2.1.7, determinism invariants).
 *
 * <p>Implementations: a {@code SecureRandom}-backed adapter for production and a seedable PRNG for
 * the simulator and golden-master tests. Implementations are <strong>not</strong> required to be
 * thread-safe: each simulator worker owns its own instance.
 */
public interface RngEngine {

    /**
     * Returns a uniformly distributed {@code int} in {@code [0, boundExclusive)}.
     *
     * @param boundExclusive exclusive upper bound, must be {@code > 0}
     * @return a value in {@code [0, boundExclusive)}
     */
    int nextInt(int boundExclusive);
}
