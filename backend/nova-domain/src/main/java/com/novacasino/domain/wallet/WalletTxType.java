package com.novacasino.domain.wallet;

/** Wallet ledger movement type. Persisted as VARCHAR + CHECK in the database. */
public enum WalletTxType {
    RECHARGE,
    BET,
    WIN
}
