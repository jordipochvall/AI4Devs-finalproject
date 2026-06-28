package com.novacasino.application.operator;

import com.novacasino.application.operator.exception.InvalidAmountException;
import com.novacasino.application.operator.exception.PlayerNotFoundException;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerSummaryDto;
import com.novacasino.common.dto.WalletDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operator player management (HU-6): paginated search and wallet recharge. Recharge idempotency is
 * provided by the web layer (it wraps this in the idempotency transaction); the amount rule lives here.
 */
public class OperatorPlayerUseCase {

    private static final Logger log = LoggerFactory.getLogger(OperatorPlayerUseCase.class);

    private final OperatorPlayerPort port;

    public OperatorPlayerUseCase(final OperatorPlayerPort port) {
        this.port = port;
    }

    @Transactional
    public PageResponse<PlayerSummaryDto> searchPlayers(final Long operatorId, final String email,
                                                        final PageRequestDto page) {
        return port.searchPlayers(operatorId, email, page);
    }

    @Transactional
    public WalletDto recharge(final Long operatorUserId, final Long operatorId,
                              final Long playerId, final Long amountCents) {
        if (amountCents == null || amountCents <= 0) {
            throw new InvalidAmountException();
        }
        final WalletDto wallet = port.recharge(operatorUserId, operatorId, playerId, amountCents)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
        log.debug("Wallet recharged: targetUserId={}, amount={}, newBalance={}, by operatorUserId={}",
                playerId, amountCents, wallet.balanceCents(), operatorUserId);
        return wallet;
    }
}
