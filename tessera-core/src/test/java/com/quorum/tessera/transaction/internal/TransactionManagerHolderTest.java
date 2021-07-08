package com.quorum.tessera.transaction.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
