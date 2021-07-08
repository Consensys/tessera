package com.quorum.tessera.transaction.internal;

import com.quorum.tessera.transaction.TransactionManager;
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
