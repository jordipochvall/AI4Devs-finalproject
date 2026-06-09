package com.novacasino.domain.rng;

/**
 * Creates a seeded {@link RngEngine} (port). Production seeds each round with a fresh value stored in
 * {@code game_rounds.rng_seed}; replay and golden-master tests re-create the engine from that seed to
 * recompute the exact same sequence (readme §2.1.7, §2.5.3).
 */
public interface RngFactory {

    /**
     * Returns a fresh {@link RngEngine} initialised with the given seed.
     *
     * @param seed the seed; the same seed yields the same sequence
     * @return a seeded engine
     */
    RngEngine create(long seed);
}
