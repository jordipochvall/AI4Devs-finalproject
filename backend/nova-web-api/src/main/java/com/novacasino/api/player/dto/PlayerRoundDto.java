package com.novacasino.api.player.dto;

import java.time.OffsetDateTime;

/**
 * One of the player's own rounds in their history (HU-14). Summary only (no RNG seed nor visual
 * result); player-scoped, so it carries no playerId.
 *
 * @param id                 round id
 * @param gameId             game played
 * @param betCents           bet of the round (0 for free-spin children)
 * @param winCents           win of this round
 * @param balancePostCents   balance after the round
 * @param freeSpin           whether this is a free-spin child round
 * @param createdAt          when the round was recorded
 */
public record PlayerRoundDto(
        Long id,
        Long gameId,
        long betCents,
        long winCents,
        long balancePostCents,
        boolean freeSpin,
        OffsetDateTime createdAt) {
}
