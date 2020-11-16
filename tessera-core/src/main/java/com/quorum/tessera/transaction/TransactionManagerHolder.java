package com.quorum.tessera.transaction;

import java.util.Optional;

enum TransactionManagerHolder {
    INSTANCE;

    private TransactionManager transactionManager;

    Optional<TransactionManager> getTransactionManager() {
        return Optional.ofNullable(transactionManager);
    }

    TransactionManager store(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        return transactionManager;
    }
}
