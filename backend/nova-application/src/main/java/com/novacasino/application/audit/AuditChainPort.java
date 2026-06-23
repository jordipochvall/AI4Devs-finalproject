package com.novacasino.application.audit;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Output port (hexagonal) for reading the auditable round chain of an operator. Implemented by an
 * adapter in {@code nova-infrastructure}; the use case depends only on this interface (HU-20).
 */
public interface AuditChainPort {

    /** Operator's rounds within [from, to] in chain order (by id), for integrity verification. */
    List<AuditRound> roundsInWindow(Long operatorId, OffsetDateTime from, OffsetDateTime to);
}
