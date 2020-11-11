package com.quorum.tessera.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManagerFactoryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerFactoryProvider.class);

    public static TransactionManagerFactory provider() {
        LOGGER.debug("Providing TransactionManagerFactory");
        TransactionManagerFactory transactionManagerFactory = DefaultTransactionManagerFactory.INSTANCE;
        LOGGER.debug("Created TransactionManagerFactory {}",transactionManagerFactory);
        return transactionManagerFactory;
    }
}
