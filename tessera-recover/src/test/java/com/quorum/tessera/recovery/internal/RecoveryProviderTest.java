package com.quorum.tessera.recovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.Test;

public class RecoveryProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new RecoveryProvider()).isNotNull();
  }

  @Test
  public void provider() {

    try (var staticStagingEntityDAO = mockStatic(StagingEntityDAO.class);
        var staticDiscovery = mockStatic(Discovery.class);
        var staticBatchTransactionRequester = mockStatic(BatchTransactionRequester.class);
        var staticTransactionManager = mockStatic(TransactionManager.class)) {

      staticStagingEntityDAO
          .when(StagingEntityDAO::create)
          .thenReturn(mock(StagingEntityDAO.class));

      staticDiscovery.when(Discovery::create).thenReturn(mock(Discovery.class));

      staticBatchTransactionRequester
          .when(BatchTransactionRequester::create)
          .thenReturn(mock(BatchTransactionRequester.class));

      TransactionManager transactionManager = mock(TransactionManager.class);
      staticTransactionManager.when(TransactionManager::create).thenReturn(transactionManager);

      Recovery recovery = RecoveryProvider.provider();

      assertThat(recovery).isNotNull().isExactlyInstanceOf(RecoveryImpl.class);

      staticStagingEntityDAO.verify(StagingEntityDAO::create);
      staticStagingEntityDAO.verifyNoMoreInteractions();

      verifyNoMoreInteractions(transactionManager);

      staticDiscovery.verify(Discovery::create);
      staticDiscovery.verifyNoMoreInteractions();
      staticBatchTransactionRequester.verify(BatchTransactionRequester::create);
      staticBatchTransactionRequester.verifyNoMoreInteractions();

      staticTransactionManager.verify(TransactionManager::create);
      staticTransactionManager.verifyNoMoreInteractions();
    }
  }
}
