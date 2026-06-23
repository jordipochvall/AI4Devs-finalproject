package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/**
 * RFJ regulatory report for a period (HU-21). Aggregates are computed from the auditable
 * {@code game_rounds} and the document references the verified integrity of the period.
 *
 * @param operatorId         the reporting operator
 * @param periodFrom         start of the period (inclusive)
 * @param periodTo           end of the period (exclusive)
 * @param totalRounds        rounds in the period
 * @param activePlayers      distinct players in the period
 * @param totalWageredCents  total wagered
 * @param totalWonCents      total paid out
 * @param ggrCents           gross gaming revenue (wagered - won)
 * @param integrityConsistent whether the period's integrity chain is intact
 * @param integrityChecked   number of rounds whose integrity was verified
 * @param generatedAt        when the report was generated
 */
public record RfjReportDto(
        Long operatorId,
        OffsetDateTime periodFrom,
        OffsetDateTime periodTo,
        long totalRounds,
        long activePlayers,
        long totalWageredCents,
        long totalWonCents,
        long ggrCents,
        boolean integrityConsistent,
        long integrityChecked,
        OffsetDateTime generatedAt) {
}
