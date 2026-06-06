package com.novacasino.infrastructure.persistence.entity;

import com.novacasino.domain.wallet.WalletTxType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Wallet ledger movement. Append-only: a DB trigger forbids UPDATE/DELETE, so this
 * entity is only ever inserted, never modified.
 */
@Entity
@Table(name = "wallet_transactions")
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletTxType type;

    /** Signed: positive for RECHARGE/WIN, negative for BET. */
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "balance_after_cents", nullable = false)
    private long balanceAfterCents;

    /** Only set for BET/WIN. */
    @Column(name = "game_round_id")
    private Long gameRoundId;

    /** Only set for RECHARGE (the operator who performed it). */
    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected WalletTransactionEntity() { }

    private WalletTransactionEntity(final Long walletId, final WalletTxType type, final long amountCents,
                                    final long balanceAfterCents, final Long gameRoundId,
                                    final Long performedByUserId) {
        this.walletId          = walletId;
        this.type              = type;
        this.amountCents       = amountCents;
        this.balanceAfterCents = balanceAfterCents;
        this.gameRoundId       = gameRoundId;
        this.performedByUserId = performedByUserId;
    }

    /**
     * Factory for a RECHARGE movement performed by an operator.
     *
     * @param walletId          target wallet
     * @param amountCents       positive amount added
     * @param balanceAfterCents resulting balance
     * @param operatorUserId    operator who performed the recharge
     */
    public static WalletTransactionEntity recharge(final Long walletId, final long amountCents,
                                                   final long balanceAfterCents, final Long operatorUserId) {
        return new WalletTransactionEntity(walletId, WalletTxType.RECHARGE, amountCents,
                balanceAfterCents, null, operatorUserId);
    }

    public Long getId()                { return id; }
    public Long getWalletId()          { return walletId; }
    public WalletTxType getType()      { return type; }
    public long getAmountCents()       { return amountCents; }
    public long getBalanceAfterCents() { return balanceAfterCents; }
    public Long getGameRoundId()       { return gameRoundId; }
    public Long getPerformedByUserId() { return performedByUserId; }
}
