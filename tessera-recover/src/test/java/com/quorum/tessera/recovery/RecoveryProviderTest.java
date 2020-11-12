package com.quorum.tessera.recovery;

import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecoveryProviderTest {

    @Test
    public void defaultConstructorForCoverage() {
        assertThat(new RecoveryProvider()).isNotNull();
    }

    @Test
    public void provider() {

        try(
            var staticStagingEntityDAO = mockStatic(StagingEntityDAO.class);
            var staticDiscovery = mockStatic(Discovery.class);
            var staticBatchTransactionRequester = mockStatic(BatchTransactionRequester.class);
            var staticTransactionManagerFactory = mockStatic(TransactionManagerFactory.class)

        ) {

            staticStagingEntityDAO.when(StagingEntityDAO::create)
                .thenReturn(mock(StagingEntityDAO.class));

            staticDiscovery.when(Discovery::getInstance)
                .thenReturn(mock(Discovery.class));

            staticBatchTransactionRequester.when(BatchTransactionRequester::create)
                .thenReturn(mock(BatchTransactionRequester.class));

            TransactionManagerFactory transactionManagerFactory = mock(TransactionManagerFactory.class);
            when(transactionManagerFactory.transactionManager()).thenReturn(Optional.of(mock(TransactionManager.class)));
            staticTransactionManagerFactory.when(TransactionManagerFactory::create)
                .thenReturn(transactionManagerFactory);

            Recovery recovery = RecoveryProvider.provider();

            assertThat(recovery).isNotNull().isExactlyInstanceOf(RecoveryImpl.class);

            staticStagingEntityDAO.verify(StagingEntityDAO::create);
            staticStagingEntityDAO.verifyNoMoreInteractions();

            verify(transactionManagerFactory).transactionManager();
            verifyNoMoreInteractions(transactionManagerFactory);

            staticDiscovery.verify(Discovery::getInstance);
            staticDiscovery.verifyNoMoreInteractions();
            staticBatchTransactionRequester.verify(BatchTransactionRequester::create);
            staticBatchTransactionRequester.verifyNoMoreInteractions();

            staticTransactionManagerFactory.verify(TransactionManagerFactory::create);
            staticTransactionManagerFactory.verifyNoMoreInteractions();

        }

    }


}
