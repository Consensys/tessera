package com.quorum.tessera.transaction;

public class TransactionManagerFactoryProvider {

    public static TransactionManagerFactory provider() {
        return DefaultTransactionManagerFactory.INSTANCE;
    }
}
