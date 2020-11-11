package com.quorum.tessera.transaction;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionManagerFactoryProviderTest {

    @Test
    public void provide() {
        TransactionManagerFactory transactionManagerFactory = TransactionManagerFactoryProvider.provider();
        assertThat(transactionManagerFactory).isNotNull().isInstanceOf(DefaultTransactionManagerFactory.class);
    }

    @Test
    public void defaultConstructorForCoverage() {
        assertThat(new TransactionManagerFactoryProvider()).isNotNull();
    }

}
