package com.quorum.tessera.transaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TransactionManagerHolderTest {

    @Before
    @After
    public void clear() {
        TransactionManagerHolder.INSTANCE.store(null);
    }

    @Test
    public void storeAndGet() {
        TransactionManagerHolder transactionManagerHolder = TransactionManagerHolder.INSTANCE;
        assertThat(transactionManagerHolder.getTransactionManager()).isNotPresent();
        TransactionManager transactionManager = mock(TransactionManager.class);
        assertThat(transactionManagerHolder.store(transactionManager)).isSameAs(transactionManager);
        assertThat(transactionManagerHolder.getTransactionManager()).containsSame(transactionManager);
    }

}
