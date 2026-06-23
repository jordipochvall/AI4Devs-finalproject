package com.novacasino.application.operator.exception;

/**
 * The RFJ report cannot be generated because the period's integrity chain is broken (HU-21). The
 * first broken round is carried for the response. Maps to HTTP 422.
 */
public class ReportIntegrityException extends RuntimeException {
    private final Long firstBrokenRoundId;

    public ReportIntegrityException(final Long firstBrokenRoundId) {
        super("Cannot generate report: integrity broken at round " + firstBrokenRoundId);
        this.firstBrokenRoundId = firstBrokenRoundId;
    }

    public Long getFirstBrokenRoundId() {
        return firstBrokenRoundId;
    }
}
