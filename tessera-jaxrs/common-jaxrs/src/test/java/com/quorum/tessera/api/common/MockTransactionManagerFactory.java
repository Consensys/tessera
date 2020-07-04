package com.quorum.tessera.api.common;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class MockTransactionManagerFactory implements TransactionManagerFactory {

    private static TransactionManager transactionManager = mock(TransactionManager.class);

    @Override
    public TransactionManager create(Config config) {
        return transactionManager;
    }

    @Override
    public Optional<TransactionManager> transactionManager() {
        return Optional.ofNullable(transactionManager);
    }

    static void setTransactionManager(TransactionManager tm) {
        transactionManager = tm;
    }
}
